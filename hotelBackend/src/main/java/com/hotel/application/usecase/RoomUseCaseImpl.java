package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateRoomRequest;
import com.hotel.application.dto.request.UpdateRoomRequest;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.domain.exception.RoomAlreadyExistsException;
import com.hotel.domain.exception.RoomNotFoundException;
import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.repository.RoomRepository;
import com.hotel.domain.repository.TariffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — RoomUseCase Implementation.
 *
 * RÈGLE DE PRIX à la création d'une chambre (si l'admin ne saisit pas de prix) :
 *
 *   1. Tarif saisonnier actif aujourd'hui pour ce type → on prend son prix effectif.
 *   2. Sinon → on prend le prix moyen des chambres existantes du même type
 *              (= le "prix de base" affiché dans AdminTariffs → Prix de base).
 *   3. Sinon (aucune chambre du type encore) → on utilise un prix par défaut selon le type.
 *              CORRIGÉ — Bug FAMILIALE : auparavant levait une IllegalStateException,
 *              ce qui rendait impossible la création d'une première chambre Familiale.
 *
 * À la MODIFICATION, si aucun prix n'est fourni :
 *   - Tarif actif → son prix effectif.
 *   - Sinon → on conserve le prix existant de la chambre.
 */
@Service
@Transactional
public class RoomUseCaseImpl implements RoomUseCase {

    private static final Logger log = LoggerFactory.getLogger(RoomUseCaseImpl.class);

    // CORRIGÉ — Bug FAMILIALE : prix par défaut par type utilisé quand aucun
    // tarif ni chambre existante ne permet de déterminer le prix.
    // Ces valeurs sont des fallbacks uniquement — l'admin peut les écraser
    // ensuite via Tarifs → Prix de base → Appliquer.
    private static final Map<RoomType, BigDecimal> DEFAULT_PRICES = Map.of(
        RoomType.SIMPLE,    BigDecimal.valueOf(100.00),
        RoomType.DOUBLE,    BigDecimal.valueOf(150.00),
        RoomType.SUITE,     BigDecimal.valueOf(500.00),
        RoomType.DELUXE,    BigDecimal.valueOf(300.00),
        RoomType.FAMILIALE, BigDecimal.valueOf(200.00)
    );

    private final RoomRepository   roomRepository;
    private final TariffRepository tariffRepository;

    public RoomUseCaseImpl(RoomRepository roomRepository, TariffRepository tariffRepository) {
        this.roomRepository  = roomRepository;
        this.tariffRepository = tariffRepository;
    }

    // ────────────────── Résolution du prix ──────────────────

    /**
     * Cherche le tarif saisonnier actif pour aujourd'hui correspondant au type donné.
     * Priorité : tarif spécifique au type > tarif générique (roomType null).
     */
    private Optional<BigDecimal> findActiveTariffPrice(RoomType type) {
        LocalDate today = LocalDate.now();
        return tariffRepository.findByActive(true)
                .stream()
                .filter(t -> !today.isBefore(t.getStartDate()) && !today.isAfter(t.getEndDate()))
                .filter(t -> t.getRoomType() == null || t.getRoomType() == type)
                .min(Comparator
                        .<Tariff, Integer>comparing(t -> t.getRoomType() == null ? 1 : 0)
                        .thenComparing(Tariff::effectivePrice))
                .map(Tariff::effectivePrice);
    }

    /**
     * Calcule le prix de base d'un type = moyenne des prix des chambres existantes du même type.
     * C'est exactement ce qu'affiche AdminTariffs → "Prix de base par type de chambre".
     */
    private Optional<BigDecimal> findBasePriceFromExistingRooms(RoomType type) {
        List<Room> siblings = roomRepository.findAllByType(type)
                .stream()
                .filter(r -> r.getPrice() != null && r.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (siblings.isEmpty()) return Optional.empty();

        BigDecimal sum = siblings.stream()
                .map(Room::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(siblings.size()), 2, RoundingMode.HALF_UP);
        return Optional.of(avg);
    }

    /**
     * Résoud le prix pour la CRÉATION d'une chambre :
     *  1. Prix explicite fourni → on l'utilise.
     *  2. Tarif saisonnier actif → son prix effectif.
     *  3. Prix de base des chambres existantes du même type → on l'utilise.
     *  4. CORRIGÉ — Bug FAMILIALE : prix par défaut selon le type (au lieu de lever
     *     une IllegalStateException qui bloquait la création de la première chambre Familiale).
     */
    private BigDecimal resolvePriceForCreate(BigDecimal explicitPrice, RoomType type) {
        if (explicitPrice != null) {
            log.info("Création chambre type={} : prix explicite={}", type, explicitPrice);
            return explicitPrice;
        }

        // Étape 2 : tarif saisonnier actif
        Optional<BigDecimal> tariffPrice = findActiveTariffPrice(type);
        if (tariffPrice.isPresent()) {
            log.info("Création chambre type={} : prix depuis tarif actif={}", type, tariffPrice.get());
            return tariffPrice.get();
        }

        // Étape 3 : prix de base des chambres existantes du même type
        Optional<BigDecimal> basePrice = findBasePriceFromExistingRooms(type);
        if (basePrice.isPresent()) {
            log.info("Création chambre type={} : prix de base depuis chambres existantes={}", type, basePrice.get());
            return basePrice.get();
        }

        // Étape 4 — CORRIGÉ : prix par défaut selon le type (plus d'exception).
        // Avant ce correctif, une IllegalStateException était levée ici, rendant
        // impossible la création de la première chambre d'un nouveau type (ex: FAMILIALE).
        BigDecimal fallback = DEFAULT_PRICES.getOrDefault(type, BigDecimal.valueOf(100.00));
        log.warn("Création chambre type={} : aucun prix trouvé, utilisation du prix par défaut={}. " +
                 "Pensez à définir un prix de base dans Tarifs → Prix de base.", type, fallback);
        return fallback;
    }

    /**
     * Résoud le prix pour la MODIFICATION d'une chambre :
     *  1. Prix explicite fourni → on l'utilise.
     *  2. Tarif saisonnier actif → son prix effectif.
     *  3. On conserve le prix existant de la chambre (jamais null).
     */
    private BigDecimal resolvePriceForUpdate(BigDecimal explicitPrice, RoomType type, BigDecimal currentPrice) {
        if (explicitPrice != null) return explicitPrice;

        Optional<BigDecimal> tariffPrice = findActiveTariffPrice(type);
        if (tariffPrice.isPresent()) {
            log.info("Mise à jour chambre type={} : prix depuis tarif actif={}", type, tariffPrice.get());
            return tariffPrice.get();
        }

        // Pas de tarif actif → on conserve le prix existant
        log.info("Mise à jour chambre type={} : pas de tarif actif, prix conservé={}", type, currentPrice);
        return currentPrice;
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByType(RoomType type) {
        return roomRepository.findAllByType(type).stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        return RoomResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findAllAvailable().stream()
                .filter(r -> r.getStatus() != RoomStatus.MAINTENANCE)
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsByType(RoomType type) {
        return roomRepository.findAvailableByType(type).stream()
                .filter(r -> r.getStatus() != RoomStatus.MAINTENANCE)
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailableForDates(checkIn, checkOut).stream()
                .filter(r -> r.getStatus() != RoomStatus.MAINTENANCE)
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRoomsForDatesByType(LocalDate checkIn,
                                                               LocalDate checkOut,
                                                               RoomType type) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailableForDatesByType(checkIn, checkOut, type).stream()
                .filter(r -> r.getStatus() != RoomStatus.MAINTENANCE)
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    // ────────────────── Commands ──────────────────

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByNumber(request.number())) {
            throw new RoomAlreadyExistsException(request.number());
        }

        List<String> amenities = request.amenities() != null
                ? new ArrayList<>(request.amenities())
                : new ArrayList<>();

        BigDecimal price = resolvePriceForCreate(request.price(), request.type());

        Room room = Room.create(
                request.number(),
                request.type(),
                price,
                request.capacity(),
                request.description(),
                amenities
        );

        Room saved = roomRepository.save(room);
        log.info("Chambre créée — id={}, number={}, type={}, price={}",
                saved.getId(), saved.getNumber(), saved.getType(), saved.getPrice());
        return RoomResponse.from(saved);
    }

    @Override
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = findOrThrow(id);

        if (!room.getNumber().equalsIgnoreCase(request.number())
                && roomRepository.existsByNumber(request.number())) {
            throw new RoomAlreadyExistsException(request.number());
        }

        List<String> amenities = request.amenities() != null
                ? new ArrayList<>(request.amenities())
                : new ArrayList<>();

        BigDecimal price = resolvePriceForUpdate(request.price(), request.type(), room.getPrice());

        room.updateDetails(
                request.number(),
                request.type(),
                price,
                request.capacity(),
                request.description(),
                amenities
        );

        if (request.status() == RoomStatus.OCCUPIED && room.getStatus() != RoomStatus.OCCUPIED) {
            room.markAsOccupied();
        } else if (request.status() == RoomStatus.AVAILABLE && room.getStatus() != RoomStatus.AVAILABLE) {
            room.markAsAvailable();
        } else if (request.status() == RoomStatus.MAINTENANCE) {
            room.markAsMaintenance();
        }

        Room updated = roomRepository.save(room);
        log.info("Chambre mise à jour — id={}, price={}", updated.getId(), updated.getPrice());
        return RoomResponse.from(updated);
    }

    @Override
    public void deleteRoom(Long id) {
        findOrThrow(id);
        roomRepository.deleteById(id);
        log.info("Chambre supprimée — id={}", id);
    }

    // ────────────────── Helpers ──────────────────

    private Room findOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null)
            throw new IllegalArgumentException("Les dates d'arrivée et de départ sont obligatoires");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("La date de départ doit être après la date d'arrivée");
    }
}
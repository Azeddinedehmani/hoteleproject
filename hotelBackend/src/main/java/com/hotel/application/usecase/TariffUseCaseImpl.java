package com.hotel.application.usecase;

import com.hotel.application.dto.request.ApplyDiscountRequest;
import com.hotel.application.dto.request.CreateTariffRequest;
import com.hotel.application.dto.request.UpdateTariffRequest;
import com.hotel.application.dto.response.TariffResponse;
import com.hotel.domain.exception.TariffNotFoundException;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * APPLICATION LAYER — Tariff Use Case Implementation.
 *
 * CORRECTION #4 : implémentation de getApplicableTariff.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TariffUseCaseImpl implements TariffUseCase {

    private final TariffRepository tariffRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TariffResponse> getAllTariffs() {
        log.debug("Fetching all tariffs");
        return tariffRepository.findAll()
                .stream()
                .map(TariffResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TariffResponse getTariffById(Long id) {
        return TariffResponse.from(findOrThrow(id));
    }

    @Override
    public TariffResponse createTariff(CreateTariffRequest request) {
        log.debug("Creating tariff: {}", request.name());
        BigDecimal discount = request.discount_percent() != null
                ? request.discount_percent()
                : BigDecimal.ZERO;

        Tariff tariff = Tariff.create(
                request.name(),
                request.season(),
                request.room_type(),
                request.price_per_night(),
                discount,
                request.start_date(),
                request.end_date()
        );
        return TariffResponse.from(tariffRepository.save(tariff));
    }

    @Override
    public TariffResponse updateTariff(Long id, UpdateTariffRequest request) {
        log.debug("Updating tariff id={}", id);
        Tariff tariff = findOrThrow(id);
        BigDecimal discount = request.discount_percent() != null
                ? request.discount_percent()
                : BigDecimal.ZERO;
        boolean active = request.is_active() != null ? request.is_active() : tariff.isActive();

        tariff.update(
                request.name(),
                request.season(),
                request.room_type(),
                request.price_per_night(),
                discount,
                request.start_date(),
                request.end_date(),
                active
        );
        return TariffResponse.from(tariffRepository.save(tariff));
    }

    @Override
    public TariffResponse applyDiscount(Long id, ApplyDiscountRequest request) {
        log.debug("Applying discount {}% to tariff id={}", request.discount(), id);
        Tariff tariff = findOrThrow(id);
        tariff.applyDiscount(request.discount());
        return TariffResponse.from(tariffRepository.save(tariff));
    }

    @Override
    public void deleteTariff(Long id) {
        log.debug("Deleting tariff id={}", id);
        if (!tariffRepository.existsById(id)) {
            throw new TariffNotFoundException("Tarif introuvable: " + id);
        }
        tariffRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECTION #4 — Tarif saisonnier applicable
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le tarif actif qui couvre la période checkIn..checkOut
     * pour le type de chambre donné.
     *
     * Algorithme :
     *  1. Charger tous les tarifs actifs.
     *  2. Filtrer ceux dont la fenêtre (startDate..endDate) couvre
     *     intégralement la période du séjour (checkIn >= startDate && checkOut <= endDate).
     *  3. Parmi les candidats, ne garder que ceux dont le roomType correspond
     *     au type demandé OU dont roomType est null (tarif générique).
     *  4. Trier : tarif spécifique (roomType non null) avant générique,
     *     puis prix effectif croissant — on prend le premier.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TariffResponse> getApplicableTariff(RoomType roomType,
                                                         LocalDate checkIn,
                                                         LocalDate checkOut) {
        log.debug("Looking for applicable tariff: roomType={}, checkIn={}, checkOut={}",
                roomType, checkIn, checkOut);

        return tariffRepository.findByActive(true)
                .stream()
                // La fenêtre tarifaire doit couvrir intégralement le séjour
                .filter(t -> !checkIn.isBefore(t.getStartDate()) && !checkOut.isAfter(t.getEndDate()))
                // Le tarif doit correspondre au type de chambre (ou être générique)
                .filter(t -> t.getRoomType() == null || t.getRoomType() == roomType)
                // Priorité : spécifique > générique, puis prix effectif le plus bas
                .min(Comparator
                        .<Tariff, Integer>comparing(t -> t.getRoomType() == null ? 1 : 0)
                        .thenComparing(Tariff::effectivePrice))
                .map(TariffResponse::from);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Tariff findOrThrow(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new TariffNotFoundException("Tarif introuvable: " + id));
    }
}
package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateReservationRequest;
import com.hotel.application.dto.request.UpdateReservationRequest;
import com.hotel.application.dto.response.ClientResponse;
import com.hotel.application.dto.response.ReservationResponse;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.application.dto.response.TariffResponse;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.exception.ReservationNotFoundException;
import com.hotel.domain.exception.RoomNotAvailableException;
import com.hotel.domain.exception.RoomNotFoundException;
import com.hotel.domain.model.Client;
import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.domain.repository.RoomRepository;
import com.hotel.infrastructure.notification.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — Implémentation du use-case ReservationUseCase.
 *
 * Toute la logique métier est ici ; les contrôleurs ne font que déléguer.
 *
 * POINT 10 — Réservation par type de chambre :
 *   La méthode {@link #createReservation} gère deux cas :
 *   <ul>
 *     <li>CLIENT : {@code roomId} est null, {@code roomType} obligatoire.
 *         La chambre précise est attribuée par la réception au check-in.
 *         On stocke roomId = null dans la réservation initiale
 *         (la réservation PENDING n'est pas encore liée à une chambre physique).
 *         → Un placeholder de chambre fictif n'est PAS utilisé ;
 *           le roomId est alimenté lors du check-in par la réception.</li>
 *     <li>ADMIN / RECEPTIONNISTE : {@code roomId} fourni → chambre précise ciblée.</li>
 *   </ul>
 *
 * POINT 11 — Validation des chevauchements par type :
 *   Quand {@code roomType} est fourni et {@code roomId} est null, on vérifie
 *   qu'il reste au moins une chambre du type demandé libre sur les dates.
 *   La vérification est déléguée au port {@link ReservationRepository#allRoomsOccupiedForType}.
 *
 * CORRECTION — Calcul automatique de appliedPrice :
 *   Le prix par nuit est calculé côté backend via {@link TariffUseCase#getApplicableTariff}.
 *   Le appliedPrice envoyé par le frontend est ignoré.
 *   Fallback : prix de base de la chambre (chemin A) ou null (chemin B sans chambre précise).
 *
 * Architecture hexagonale respectée : aucune dépendance vers JPA dans cette classe.
 */
@Service
@Transactional
public class ReservationUseCaseImpl implements ReservationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservationUseCaseImpl.class);

    /** Nombre de jours minimum avant le check-in pour annuler gratuitement. */
    private static final int MIN_DAYS_BEFORE_CHECKIN = 2;

    private final ReservationRepository    reservationRepository;
    private final ClientRepository         clientRepository;
    private final RoomRepository           roomRepository;
    private final InvoiceUseCase           invoiceUseCase;
    private final TariffUseCase            tariffUseCase;
    private final EmailNotificationService emailNotificationService;

    public ReservationUseCaseImpl(ReservationRepository reservationRepository,
                                  ClientRepository clientRepository,
                                  RoomRepository roomRepository,
                                  InvoiceUseCase invoiceUseCase,
                                  TariffUseCase tariffUseCase,
                                  EmailNotificationService emailNotificationService) {
        this.reservationRepository    = reservationRepository;
        this.clientRepository         = clientRepository;
        this.roomRepository           = roomRepository;
        this.invoiceUseCase           = invoiceUseCase;
        this.tariffUseCase            = tariffUseCase;
        this.emailNotificationService = emailNotificationService;
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long clientId) {
        return reservationRepository.findByClientId(clientId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return enrich(findOrThrow(id));
    }

    // ────────────────── Commands ──────────────────

    /**
     * POINT 10 + POINT 11 — Création d'une réservation.
     *
     * Deux chemins :
     *
     * (A) roomId fourni (ADMIN / RECEPTIONNISTE) :
     *     – Vérifie que la chambre existe et est disponible
     *     – Vérifie l'absence de chevauchement par chambre précise
     *     – Calcule le prix via TariffUseCase ; fallback = room.getPricePerNight()
     *     – Crée la réservation liée à cette chambre
     *
     * (B) roomId null + roomType fourni (CLIENT) :
     *     – POINT 11 : vérifie via JPQL qu'au moins une chambre du type est libre
     *       → RoomNotAvailableException si toutes occupées
     *     – Calcule le prix via TariffUseCase ; fallback = request.appliedPrice() (peut être null)
     *     – Crée la réservation avec roomId = null
     *       (la chambre sera attribuée au check-in par la réception)
     */
    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // Vérification du client
        clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientNotFoundException(request.clientId()));

        if (request.roomId() != null) {
            // ── Chemin A : chambre précise (ADMIN / RECEPTIONNISTE) ──────────────
            Room room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

            if (!room.isAvailable()) {
                throw new RoomNotAvailableException(
                        request.roomId(),
                        request.checkInDate().toString(),
                        request.checkOutDate().toString());
            }

            // POINT 11 : vérification chevauchement pour cette chambre précise
            if (reservationRepository.hasOverlappingReservation(
                    request.roomId(), request.checkInDate(), request.checkOutDate(), null)) {
                throw new RoomNotAvailableException(
                        request.roomId(),
                        request.checkInDate().toString(),
                        request.checkOutDate().toString());
            }

            // CORRECTION — Calcul automatique du prix via TariffUseCase
            // Le roomType de la chambre est utilisé pour chercher le tarif applicable
            RoomType roomType = room.getType();
            BigDecimal appliedPrice = resolvePrice(
                    roomType,
                    request.checkInDate(),
                    request.checkOutDate(),
                    room.getPrice()           // fallback = prix de base de la chambre
            );

            Reservation reservation = Reservation.create(
                    request.clientId(), request.roomId(),
                    request.checkInDate(), request.checkOutDate(),
                    request.guests(), request.notes(), appliedPrice);

            Reservation saved = reservationRepository.save(reservation);
            log.info("Réservation créée (chambre précise) — id={}, roomId={}, appliedPrice={}",
                    saved.getId(), saved.getRoomId(), appliedPrice);

            clientRepository.findById(saved.getClientId()).ifPresent(client ->
                    emailNotificationService.sendReservationReceived(client, saved));

            return enrich(saved);

        } else {
            // ── Chemin B : par type de chambre (CLIENT) ──────────────────────────

            RoomType roomType = request.roomType();
            if (roomType == null) {
                // Sécurité : ne devrait pas arriver si la validation Bean Validation est active
                throw new IllegalArgumentException(
                        "roomType est obligatoire quand aucun roomId n'est fourni.");
            }

            // POINT 11 — Vérification des chevauchements par type via JPQL
            boolean toutesOccupees = reservationRepository.allRoomsOccupiedForType(
                    roomType,
                    request.checkInDate(),
                    request.checkOutDate(),
                    null);

            if (toutesOccupees) {
                // Aucune chambre du type demandé n'est libre → exception orientée client
                throw new RoomNotAvailableException(
                        roomType,
                        request.checkInDate().toString(),
                        request.checkOutDate().toString());
            }

            // CORRECTION — Calcul automatique du prix via TariffUseCase
            // Fallback = request.appliedPrice() si fourni, sinon null (chambre pas encore connue)
            BigDecimal appliedPrice = resolvePrice(
                    roomType,
                    request.checkInDate(),
                    request.checkOutDate(),
                    request.appliedPrice()    // fallback = valeur frontend (nullable)
            );

            // Création de la réservation sans roomId précis (null)
            // La chambre sera attribuée par la réception lors du check-in
            Reservation reservation = Reservation.createByType(
                    request.clientId(),
                    roomType,
                    request.checkInDate(),
                    request.checkOutDate(),
                    request.guests(),
                    request.notes(),
                    appliedPrice);

            Reservation saved = reservationRepository.save(reservation);
            log.info("Réservation créée (par type) — id={}, roomType={}, appliedPrice={}",
                    saved.getId(), roomType, appliedPrice);

            clientRepository.findById(saved.getClientId()).ifPresent(client ->
                    emailNotificationService.sendReservationReceived(client, saved));

            return enrich(saved);
        }
    }

    @Override
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = findOrThrow(id);

        // Vérification chevauchement uniquement si une chambre précise est déjà attribuée
        if (reservation.getRoomId() != null) {
            if (reservationRepository.hasOverlappingReservation(
                    reservation.getRoomId(), request.checkInDate(), request.checkOutDate(), id)) {
                throw new RoomNotAvailableException(
                        reservation.getRoomId(),
                        request.checkInDate().toString(),
                        request.checkOutDate().toString());
            }
        }

        reservation.updateDetails(request.checkInDate(), request.checkOutDate(),
                request.guests(), request.notes());

        if (request.status() != null) {
            applyStatusTransition(reservation, request.status());
        }

        Reservation updated = reservationRepository.save(reservation);
        log.info("Réservation mise à jour — id={}", updated.getId());
        return enrich(updated);
    }

    @Override
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = findOrThrow(id);
        reservation.canBeCancelledBy(LocalDate.now(), MIN_DAYS_BEFORE_CHECKIN);
        reservation.cancel();
        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation annulée — id={}", id);
        return enrich(saved);
    }

    /**
     * ASSIGN ROOM — attribue une chambre physique à une réservation dont roomId est null.
     * Appelé depuis la page Check-in/out avant de lancer le check-in.
     */
    @Override
    public ReservationResponse assignRoom(Long id, Long roomId) {
        Reservation reservation = findOrThrow(id);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (!room.isAvailable()) {
            throw new RoomNotAvailableException(roomId,
                    reservation.getCheckInDate().toString(),
                    reservation.getCheckOutDate().toString());
        }

        reservation.assignRoom(roomId);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Chambre attribuée — reservationId={}, roomId={}", saved.getId(), roomId);
        return enrich(saved);
    }

    /**
     * CHECK-IN — la réception attribue la chambre précise.
     *
     * POINT 10 : si {@code roomId} est null dans la réservation (réservation par type),
     * la réception doit attribuer une chambre disponible du type correspondant.
     * Cette méthode est appelée via l'interface ADMIN/RECEPTIONNISTE qui transmet
     * le roomId via {@link UpdateReservationRequest} avant ou pendant le check-in.
     *
     * Si roomId est toujours null au moment du check-in, une {@link RoomNotFoundException}
     * est levée pour indiquer à la réception qu'elle doit d'abord attribuer une chambre.
     */
    @Override
    public ReservationResponse checkIn(Long id) {
        Reservation reservation = findOrThrow(id);

        if (reservation.getRoomId() == null) {
            throw new IllegalStateException(
                    "Impossible d'effectuer le check-in : aucune chambre n'a encore été attribuée "
                    + "à cette réservation. Veuillez attribuer une chambre depuis l'interface de réception.");
        }

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));

        reservation.checkIn();
        room.markAsOccupied();
        roomRepository.save(room);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Check-in effectué — reservationId={}, roomId={}, at={}",
                saved.getId(), room.getId(), saved.getActualCheckInAt());
        return enrich(saved);
    }

    /**
     * CHECK-OUT avec génération automatique de la facture.
     * FIX : si roomId est null (ne devrait pas arriver, mais par sécurité), on lève une erreur claire.
     */
    @Override
    public ReservationResponse checkOut(Long id) {
        Reservation reservation = findOrThrow(id);

        if (reservation.getRoomId() == null) {
            throw new IllegalStateException(
                    "Impossible d'effectuer le check-out : aucune chambre n'est attribuée à cette réservation.");
        }

        reservation.checkOut();

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));
        room.markAsAvailable();
        roomRepository.save(room);

        Reservation saved = reservationRepository.save(reservation);

        long       nights        = saved.getDurationNights();
        BigDecimal pricePerNight = room.getPrice();
        BigDecimal discountRate  = BigDecimal.ZERO;

        invoiceUseCase.generateInvoice(saved.getId(), nights, pricePerNight, discountRate);

        log.info("Check-out effectué + facture générée — reservationId={}, roomId={}, nights={}, total={}",
                saved.getId(), room.getId(), nights,
                pricePerNight.multiply(BigDecimal.valueOf(nights)));

        return enrich(saved);
    }

    @Override
    public void deleteReservation(Long id) {
        findOrThrow(id);
        reservationRepository.deleteById(id);
        log.info("Réservation supprimée — id={}", id);
    }

    // ────────────────── Helpers ──────────────────

    private Reservation findOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    /**
     * CORRECTION — Calcul du prix applicable.
     *
     * Priorité :
     *  1. Tarif actif trouvé via TariffUseCase (effective_price = pricePerNight * (1 - discount/100))
     *  2. fallbackPrice si aucun tarif actif ne couvre la période (prix de base chambre ou null)
     */
    private BigDecimal resolvePrice(RoomType roomType,
                                    LocalDate checkIn,
                                    LocalDate checkOut,
                                    BigDecimal fallbackPrice) {
        Optional<TariffResponse> tariff = tariffUseCase.getApplicableTariff(roomType, checkIn, checkOut);
        if (tariff.isPresent()) {
            log.debug("Tarif applicable trouvé — tariffId={}, effectivePrice={}",
                    tariff.get().id(), tariff.get().effective_price());
            return tariff.get().effective_price();
        }
        log.debug("Aucun tarif applicable trouvé pour roomType={} [{} → {}] — fallback={}",
                roomType, checkIn, checkOut, fallbackPrice);
        return fallbackPrice;
    }

    /**
     * CORRECTION — Enrichissement avec totalPrice recalculé.
     *
     * totalPrice = nights * appliedPrice (si appliedPrice est connu).
     */
    private ReservationResponse enrich(Reservation r) {
        ClientResponse client = clientRepository.findById(r.getClientId())
                .map(ClientResponse::from).orElse(null);
        // roomId peut être null (réservation par type non encore attribuée)
        RoomResponse room = r.getRoomId() != null
                ? roomRepository.findById(r.getRoomId()).map(RoomResponse::from).orElse(null)
                : null;

        // Calcul du totalPrice : nights * appliedPrice
        BigDecimal totalPrice = null;
        if (r.getAppliedPrice() != null) {
            totalPrice = r.getAppliedPrice().multiply(BigDecimal.valueOf(r.getDurationNights()));
        }

        return ReservationResponse.from(r, client, room, totalPrice);
    }

    private void applyStatusTransition(Reservation reservation, ReservationStatus target) {
        switch (target) {
            case CONFIRMED   -> {
                reservation.confirm();
                clientRepository.findById(reservation.getClientId()).ifPresent(client ->
                        emailNotificationService.sendReservationConfirmed(client, reservation));
            }
            case CANCELLED   -> reservation.cancel();
            case CHECKED_IN  -> reservation.checkIn();
            case CHECKED_OUT -> reservation.checkOut();
            default -> { /* PENDING — état initial */ }
        }
    }
}
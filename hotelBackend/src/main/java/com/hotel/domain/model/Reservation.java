package com.hotel.domain.model;

import com.hotel.domain.exception.ReservationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DOMAIN LAYER — Aggregate Root Reservation (v3 — réservation par type de chambre)
 *
 * POINT 10 — La chambre précise ({@code roomId}) peut être null au moment de la
 * création si le client réserve par type. Elle sera attribuée par la réception
 * lors du check-in. Le champ {@code roomType} capture le type demandé.
 *
 * Invariants :
 * <ul>
 *   <li>Un seul parmi {@code roomId} ou {@code roomType} est obligatoire à la création.</li>
 *   <li>{@code roomId} est obligatoire au check-in (la réception doit avoir attribué une chambre).</li>
 *   <li>Les dates et le nombre de personnes restent invariants (validation dans les factory methods).</li>
 * </ul>
 */
public class Reservation {

    private Long              id;
    private Long              clientId;
    /** Null jusqu'à l'attribution de chambre par la réception (réservation par type). */
    private Long              roomId;
    /** Type de chambre demandé par le client. Null si roomId est fourni directement. */
    private RoomType          roomType;
    private LocalDate         checkInDate;
    private LocalDate         checkOutDate;
    private ReservationStatus status;
    private int               guests;
    private String            notes;
    private LocalDateTime     actualCheckInAt;
    private LocalDateTime     actualCheckOutAt;
    /**
     * Prix appliqué par nuit au moment de la création de la réservation.
     * Calculé automatiquement par le backend via TariffUseCase.
     * Null si aucun tarif actif ne couvre la période (fallback = prix de base de la chambre).
     */
    private BigDecimal        appliedPrice;

    protected Reservation() {}

    private Reservation(Builder b) {
        this.id               = b.id;
        this.clientId         = b.clientId;
        this.roomId           = b.roomId;
        this.roomType         = b.roomType;
        this.checkInDate      = b.checkInDate;
        this.checkOutDate     = b.checkOutDate;
        this.status           = b.status;
        this.guests           = b.guests;
        this.notes            = b.notes;
        this.actualCheckInAt  = b.actualCheckInAt;
        this.actualCheckOutAt = b.actualCheckOutAt;
        this.appliedPrice     = b.appliedPrice;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Réservation par chambre précise — usage ADMIN / RECEPTIONNISTE.
     */
    public static Reservation create(Long clientId, Long roomId,
                                     LocalDate checkInDate, LocalDate checkOutDate,
                                     int guests, String notes) {
        validateClientId(clientId);
        validateRoomId(roomId);
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        return new Builder()
                .clientId(clientId).roomId(roomId)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(ReservationStatus.PENDING).guests(guests).notes(notes)
                .build();
    }

    /**
     * Réservation par chambre précise avec prix appliqué calculé par le backend.
     */
    public static Reservation create(Long clientId, Long roomId,
                                     LocalDate checkInDate, LocalDate checkOutDate,
                                     int guests, String notes, BigDecimal appliedPrice) {
        validateClientId(clientId);
        validateRoomId(roomId);
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        return new Builder()
                .clientId(clientId).roomId(roomId)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(ReservationStatus.PENDING).guests(guests).notes(notes)
                .appliedPrice(appliedPrice)
                .build();
    }

    /**
     * POINT 10 — Réservation par type de chambre — usage CLIENT.
     *
     * {@code roomId} est null : la chambre physique sera attribuée par la réception
     * lors du check-in. Seul {@code roomType} identifie la catégorie demandée.
     */
    public static Reservation createByType(Long clientId, RoomType roomType,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           int guests, String notes) {
        validateClientId(clientId);
        if (roomType == null) throw new IllegalArgumentException("Le type de chambre est obligatoire");
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        return new Builder()
                .clientId(clientId)
                .roomId(null)           // chambre non encore attribuée
                .roomType(roomType)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(ReservationStatus.PENDING).guests(guests).notes(notes)
                .build();
    }

    /**
     * POINT 10 — Réservation par type de chambre avec prix appliqué calculé par le backend.
     */
    public static Reservation createByType(Long clientId, RoomType roomType,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           int guests, String notes, BigDecimal appliedPrice) {
        validateClientId(clientId);
        if (roomType == null) throw new IllegalArgumentException("Le type de chambre est obligatoire");
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        return new Builder()
                .clientId(clientId)
                .roomId(null)
                .roomType(roomType)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(ReservationStatus.PENDING).guests(guests).notes(notes)
                .appliedPrice(appliedPrice)
                .build();
    }

    /**
     * Reconstitution depuis la persistence (infrastructure uniquement).
     */
    public static Reservation reconstitute(Long id, Long clientId, Long roomId, RoomType roomType,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           ReservationStatus status, int guests, String notes,
                                           LocalDateTime actualCheckInAt,
                                           LocalDateTime actualCheckOutAt) {
        return new Builder()
                .id(id).clientId(clientId).roomId(roomId).roomType(roomType)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(status).guests(guests).notes(notes)
                .actualCheckInAt(actualCheckInAt).actualCheckOutAt(actualCheckOutAt)
                .build();
    }

    /**
     * Reconstitution complète depuis la persistence avec prix appliqué.
     */
    public static Reservation reconstitute(Long id, Long clientId, Long roomId, RoomType roomType,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           ReservationStatus status, int guests, String notes,
                                           LocalDateTime actualCheckInAt, LocalDateTime actualCheckOutAt,
                                           BigDecimal appliedPrice) {
        return new Builder()
                .id(id).clientId(clientId).roomId(roomId).roomType(roomType)
                .checkInDate(checkInDate).checkOutDate(checkOutDate)
                .status(status).guests(guests).notes(notes)
                .actualCheckInAt(actualCheckInAt).actualCheckOutAt(actualCheckOutAt)
                .appliedPrice(appliedPrice)
                .build();
    }

    /**
     * Rétrocompatibilité : reconstitution sans roomType (migrations / anciens mappers).
     */
    public static Reservation reconstitute(Long id, Long clientId, Long roomId,
                                           LocalDate checkInDate, LocalDate checkOutDate,
                                           ReservationStatus status, int guests, String notes,
                                           LocalDateTime actualCheckInAt,
                                           LocalDateTime actualCheckOutAt) {
        return reconstitute(id, clientId, roomId, null,
                checkInDate, checkOutDate, status, guests, notes,
                actualCheckInAt, actualCheckOutAt);
    }

    // ── Transitions de statut ─────────────────────────────────────────────────

    public void confirm() {
        if (this.status == ReservationStatus.CANCELLED)
            throw new IllegalStateException("Impossible de confirmer une réservation annulée");
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CHECKED_IN || this.status == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Impossible d'annuler une réservation en cours ou terminée");
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * Vérifie que la politique d'annulation est respectée.
     *
     * @param today                date du jour
     * @param minDaysBeforeCheckIn nombre minimum de jours avant le check-in
     */
    public void canBeCancelledBy(LocalDate today, int minDaysBeforeCheckIn) {
        long jours = ChronoUnit.DAYS.between(today, this.checkInDate);
        if (jours < minDaysBeforeCheckIn) {
            throw new ReservationException(
                "Annulation impossible : le délai minimum de " + minDaysBeforeCheckIn +
                " jour(s) avant l'arrivée n'est pas respecté. " +
                "Date d'arrivée : " + this.checkInDate + "."
            );
        }
    }

    /** Check-in : réservation doit être CONFIRMED — enregistre l'heure réelle. */
    public void checkIn() {
        if (this.status != ReservationStatus.CONFIRMED)
            throw new IllegalStateException(
                "La réservation doit être CONFIRMED avant le check-in (statut actuel : " + this.status + ")");
        this.status          = ReservationStatus.CHECKED_IN;
        this.actualCheckInAt = LocalDateTime.now();
    }

    /** Check-out : réservation doit être CHECKED_IN — enregistre l'heure réelle. */
    public void checkOut() {
        if (this.status != ReservationStatus.CHECKED_IN)
            throw new IllegalStateException(
                "Le client doit être en CHECKED_IN avant le check-out (statut actuel : " + this.status + ")");
        this.status           = ReservationStatus.CHECKED_OUT;
        this.actualCheckOutAt = LocalDateTime.now();
    }

    public void updateDetails(LocalDate checkInDate, LocalDate checkOutDate, int guests, String notes) {
        if (this.status == ReservationStatus.CANCELLED || this.status == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Impossible de modifier une réservation annulée ou terminée");
        validateDates(checkInDate, checkOutDate);
        validateGuests(guests);
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guests       = guests;
        this.notes        = notes;
    }

    /**
     * Attribue la chambre précise lors du check-in (réception).
     * Met à jour roomId et efface roomType (la chambre est maintenant connue).
     *
     * @param roomId identifiant de la chambre attribuée
     */
    public void assignRoom(Long roomId) {
        if (roomId == null) throw new IllegalArgumentException("L'identifiant de chambre est obligatoire");
        this.roomId   = roomId;
        this.roomType = null; // la chambre précise est connue ; le type n'est plus nécessaire
    }

    // ── Calculs ───────────────────────────────────────────────────────────────

    public long getDurationNights() { return ChronoUnit.DAYS.between(checkInDate, checkOutDate); }

    public boolean isActive() {
        return status == ReservationStatus.PENDING
            || status == ReservationStatus.CONFIRMED
            || status == ReservationStatus.CHECKED_IN;
    }

    // ── Validations privées ───────────────────────────────────────────────────

    private static void validateClientId(Long v) {
        if (v == null) throw new IllegalArgumentException("Client obligatoire");
    }
    private static void validateRoomId(Long v) {
        if (v == null) throw new IllegalArgumentException("Chambre obligatoire");
    }
    private static void validateDates(LocalDate ci, LocalDate co) {
        if (ci == null) throw new IllegalArgumentException("Date d'arrivée obligatoire");
        if (co == null) throw new IllegalArgumentException("Date de départ obligatoire");
        if (!co.isAfter(ci)) throw new IllegalArgumentException("Le départ doit être après l'arrivée");
    }
    private static void validateGuests(int g) {
        if (g < 1)  throw new IllegalArgumentException("Minimum 1 personne");
        if (g > 20) throw new IllegalArgumentException("Maximum 20 personnes");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long              getId()               { return id; }
    public Long              getClientId()         { return clientId; }
    /** Null si la chambre n'a pas encore été attribuée (réservation par type). */
    public Long              getRoomId()           { return roomId; }
    /** Type demandé par le client ; null si roomId est connu. */
    public RoomType          getRoomType()         { return roomType; }
    public LocalDate         getCheckInDate()      { return checkInDate; }
    public LocalDate         getCheckOutDate()     { return checkOutDate; }
    public ReservationStatus getStatus()           { return status; }
    public int               getGuests()           { return guests; }
    public String            getNotes()            { return notes; }
    public LocalDateTime     getActualCheckInAt()  { return actualCheckInAt; }
    public LocalDateTime     getActualCheckOutAt() { return actualCheckOutAt; }
    /** Prix par nuit calculé par le backend au moment de la réservation. Nullable. */
    public BigDecimal        getAppliedPrice()     { return appliedPrice; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {
        private Long id; private Long clientId; private Long roomId; private RoomType roomType;
        private LocalDate checkInDate; private LocalDate checkOutDate;
        private ReservationStatus status; private int guests; private String notes;
        private LocalDateTime actualCheckInAt; private LocalDateTime actualCheckOutAt;
        private BigDecimal appliedPrice;

        public Builder id(Long v)                       { this.id = v; return this; }
        public Builder clientId(Long v)                 { this.clientId = v; return this; }
        public Builder roomId(Long v)                   { this.roomId = v; return this; }
        public Builder roomType(RoomType v)             { this.roomType = v; return this; }
        public Builder checkInDate(LocalDate v)         { this.checkInDate = v; return this; }
        public Builder checkOutDate(LocalDate v)        { this.checkOutDate = v; return this; }
        public Builder status(ReservationStatus v)      { this.status = v; return this; }
        public Builder guests(int v)                    { this.guests = v; return this; }
        public Builder notes(String v)                  { this.notes = v; return this; }
        public Builder actualCheckInAt(LocalDateTime v) { this.actualCheckInAt = v; return this; }
        public Builder actualCheckOutAt(LocalDateTime v){ this.actualCheckOutAt = v; return this; }
        public Builder appliedPrice(BigDecimal v)       { this.appliedPrice = v; return this; }
        public Reservation build()                      { return new Reservation(this); }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return Objects.equals(id, r.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return "Reservation{id=" + id + ", status=" + status + "}"; }
}
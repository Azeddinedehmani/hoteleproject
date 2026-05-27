package com.hotel.infrastructure.persistence.entity;

import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.RoomType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Reservation (v3 — roomType + roomId nullable)
 *
 * POINT 10 — {@code roomId} devient nullable pour supporter les réservations par type
 * (la chambre précise est attribuée au check-in par la réception).
 * {@code roomType} stocke le type demandé par le client.
 *
 * Migration DDL (si ddl-auto != create/update) :
 *   ALTER TABLE reservations ALTER COLUMN room_id DROP NOT NULL;
 *   ALTER TABLE reservations ADD COLUMN room_type VARCHAR(20);
 */
@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /**
     * POINT 10 — Nullable : null tant que la réception n'a pas attribué de chambre.
     */
    @Column(name = "room_id")
    private Long roomId;

    /**
     * POINT 10 — Type de chambre demandé par le client.
     * Null si la chambre a été ciblée directement via roomId.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 20)
    private RoomType roomType;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private int guests;

    @Column(length = 500)
    private String notes;

    /** Heure réelle d'arrivée — null jusqu'au check-in. */
    @Column(name = "actual_check_in_at")
    private LocalDateTime actualCheckInAt;

    /** Heure réelle de départ — null jusqu'au check-out. */
    @Column(name = "actual_check_out_at")
    private LocalDateTime actualCheckOutAt;

    public ReservationEntity() {}

    private ReservationEntity(Builder b) {
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
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private Long clientId;
        private Long roomId;
        private RoomType roomType;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private ReservationStatus status;
        private int guests;
        private String notes;
        private LocalDateTime actualCheckInAt;
        private LocalDateTime actualCheckOutAt;

        public Builder id(Long v)                        { this.id = v; return this; }
        public Builder clientId(Long v)                  { this.clientId = v; return this; }
        public Builder roomId(Long v)                    { this.roomId = v; return this; }
        public Builder roomType(RoomType v)              { this.roomType = v; return this; }
        public Builder checkInDate(LocalDate v)          { this.checkInDate = v; return this; }
        public Builder checkOutDate(LocalDate v)         { this.checkOutDate = v; return this; }
        public Builder status(ReservationStatus v)       { this.status = v; return this; }
        public Builder guests(int v)                     { this.guests = v; return this; }
        public Builder notes(String v)                   { this.notes = v; return this; }
        public Builder actualCheckInAt(LocalDateTime v)  { this.actualCheckInAt = v; return this; }
        public Builder actualCheckOutAt(LocalDateTime v) { this.actualCheckOutAt = v; return this; }
        public ReservationEntity build()                 { return new ReservationEntity(this); }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long              getId()               { return id; }
    public void              setId(Long v)         { this.id = v; }

    public Long              getClientId()         { return clientId; }
    public void              setClientId(Long v)   { this.clientId = v; }

    public Long              getRoomId()           { return roomId; }
    public void              setRoomId(Long v)     { this.roomId = v; }

    public RoomType          getRoomType()         { return roomType; }
    public void              setRoomType(RoomType v) { this.roomType = v; }

    public LocalDate         getCheckInDate()      { return checkInDate; }
    public void              setCheckInDate(LocalDate v)  { this.checkInDate = v; }

    public LocalDate         getCheckOutDate()     { return checkOutDate; }
    public void              setCheckOutDate(LocalDate v) { this.checkOutDate = v; }

    public ReservationStatus getStatus()           { return status; }
    public void              setStatus(ReservationStatus v) { this.status = v; }

    public int               getGuests()           { return guests; }
    public void              setGuests(int v)      { this.guests = v; }

    public String            getNotes()            { return notes; }
    public void              setNotes(String v)    { this.notes = v; }

    public LocalDateTime     getActualCheckInAt()  { return actualCheckInAt; }
    public void              setActualCheckInAt(LocalDateTime v)  { this.actualCheckInAt = v; }

    public LocalDateTime     getActualCheckOutAt() { return actualCheckOutAt; }
    public void              setActualCheckOutAt(LocalDateTime v) { this.actualCheckOutAt = v; }
}
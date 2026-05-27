package com.hotel.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Invoice
 */
@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK vers reservations.id — unicité : 1 réservation → 1 facture */
    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    /** Nombre de nuits facturées */
    @Column(nullable = false)
    private long nights;

    /** Prix de la chambre au moment du check-out (snapshot) */
    @Column(name = "room_price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal roomPricePerNight;

    /** Taux de remise appliqué (0.00 = aucune remise) */
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;

    /** Montant total après remise */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /** Date/heure de génération de la facture */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Statut de paiement : UNPAID (par défaut) ou PAID.
     * columnDefinition permet de définir la valeur par défaut en base.
     */
    @Column(name = "status", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) DEFAULT 'UNPAID'")
    private String status = "UNPAID";

    public InvoiceEntity() {}

    // ────────────────── Getters / Setters ──────────────────

    public Long          getId()                { return id; }
    public void          setId(Long v)          { this.id = v; }

    public Long          getReservationId()     { return reservationId; }
    public void          setReservationId(Long v){ this.reservationId = v; }

    public long          getNights()            { return nights; }
    public void          setNights(long v)      { this.nights = v; }

    public BigDecimal    getRoomPricePerNight() { return roomPricePerNight; }
    public void          setRoomPricePerNight(BigDecimal v) { this.roomPricePerNight = v; }

    public BigDecimal    getDiscountRate()      { return discountRate; }
    public void          setDiscountRate(BigDecimal v) { this.discountRate = v; }

    public BigDecimal    getTotalPrice()        { return totalPrice; }
    public void          setTotalPrice(BigDecimal v) { this.totalPrice = v; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public String        getStatus()            { return status; }
    public void          setStatus(String v)    { this.status = v; }
}
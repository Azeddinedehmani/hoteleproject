package com.hotel.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DOMAIN LAYER — Invoice Aggregate Root
 * Pure Java object — no JPA, no Spring, no framework.
 *
 * Calcul : totalPrice = nights × roomPrice × (1 - discountRate)
 */
public class Invoice {

    private Long          id;
    private Long          reservationId;
    private long          nights;
    private BigDecimal    roomPricePerNight;
    private BigDecimal    discountRate;      // 0.00 → 1.00  (ex: 0.10 = 10 %)
    private BigDecimal    totalPrice;
    private LocalDateTime createdAt;
    private String        status;            // "UNPAID" | "PAID"

    protected Invoice() {}

    private Invoice(Builder b) {
        this.id                = b.id;
        this.reservationId     = b.reservationId;
        this.nights            = b.nights;
        this.roomPricePerNight = b.roomPricePerNight;
        this.discountRate      = b.discountRate;
        this.totalPrice        = b.totalPrice;
        this.createdAt         = b.createdAt;
        this.status            = b.status != null ? b.status : "UNPAID";
    }

    // ────────────────── Factory ──────────────────

    /**
     * Crée une facture au moment du check-out.
     *
     * @param reservationId  ID de la réservation
     * @param nights         nombre de nuits (checkOut - checkIn)
     * @param pricePerNight  prix de la chambre par nuit
     * @param discountRate   taux de remise (0.0 si aucune remise)
     */
    public static Invoice generate(Long reservationId,
                                   long nights,
                                   BigDecimal pricePerNight,
                                   BigDecimal discountRate) {
        validateReservationId(reservationId);
        validateNights(nights);
        validatePrice(pricePerNight);
        validateDiscount(discountRate);

        BigDecimal base  = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal total = base.multiply(BigDecimal.ONE.subtract(discountRate))
                              .setScale(2, RoundingMode.HALF_UP);

        return new Builder()
                .reservationId(reservationId)
                .nights(nights)
                .roomPricePerNight(pricePerNight)
                .discountRate(discountRate)
                .totalPrice(total)
                .createdAt(LocalDateTime.now())
                .status("UNPAID")
                .build();
    }

    /** Reconstitution depuis la persistance (pas de recalcul). */
    public static Invoice reconstitute(Long id, Long reservationId,
                                       long nights, BigDecimal roomPricePerNight,
                                       BigDecimal discountRate, BigDecimal totalPrice,
                                       LocalDateTime createdAt, String status) {
        return new Builder()
                .id(id)
                .reservationId(reservationId)
                .nights(nights)
                .roomPricePerNight(roomPricePerNight)
                .discountRate(discountRate)
                .totalPrice(totalPrice)
                .createdAt(createdAt)
                .status(status != null ? status : "UNPAID")
                .build();
    }

    // ────────────────── Commande métier ──────────────────

    /**
     * Marque la facture comme payée.
     * Lève une exception si elle est déjà payée.
     */
    public void markAsPaid() {
        if ("PAID".equals(this.status)) {
            throw new IllegalStateException("La facture #" + id + " est déjà marquée comme payée.");
        }
        this.status = "PAID";
    }

    // ────────────────── Invariants ──────────────────

    private static void validateReservationId(Long v) {
        if (v == null) throw new IllegalArgumentException("ReservationId obligatoire");
    }

    private static void validateNights(long n) {
        if (n < 1) throw new IllegalArgumentException("Une facture doit couvrir au moins 1 nuit");
    }

    private static void validatePrice(BigDecimal p) {
        if (p == null || p.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix par nuit doit être > 0");
    }

    private static void validateDiscount(BigDecimal d) {
        if (d == null || d.compareTo(BigDecimal.ZERO) < 0 || d.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException("Le taux de remise doit être compris entre 0.0 et 1.0");
    }

    // ────────────────── Getters ──────────────────

    public Long          getId()                { return id; }
    public Long          getReservationId()     { return reservationId; }
    public long          getNights()            { return nights; }
    public BigDecimal    getRoomPricePerNight() { return roomPricePerNight; }
    public BigDecimal    getDiscountRate()      { return discountRate; }
    public BigDecimal    getTotalPrice()        { return totalPrice; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public String        getStatus()            { return status; }

    // ────────────────── Builder ──────────────────

    public static final class Builder {
        private Long id; private Long reservationId;
        private long nights; private BigDecimal roomPricePerNight;
        private BigDecimal discountRate; private BigDecimal totalPrice;
        private LocalDateTime createdAt; private String status;

        public Builder id(Long v)                     { this.id = v; return this; }
        public Builder reservationId(Long v)          { this.reservationId = v; return this; }
        public Builder nights(long v)                 { this.nights = v; return this; }
        public Builder roomPricePerNight(BigDecimal v){ this.roomPricePerNight = v; return this; }
        public Builder discountRate(BigDecimal v)     { this.discountRate = v; return this; }
        public Builder totalPrice(BigDecimal v)       { this.totalPrice = v; return this; }
        public Builder createdAt(LocalDateTime v)     { this.createdAt = v; return this; }
        public Builder status(String v)               { this.status = v; return this; }
        public Invoice build()                        { return new Invoice(this); }
    }

    // ────────────────── equals / hashCode ──────────────────

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice inv)) return false;
        return Objects.equals(id, inv.id);
    }
    @Override public int    hashCode()  { return Objects.hash(id); }
    @Override public String toString()  {
        return "Invoice{id=" + id + ", reservationId=" + reservationId
               + ", total=" + totalPrice + ", status=" + status + "}";
    }
}
package com.hotel.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DOMAIN LAYER — Tariff Aggregate Root.
 * Represents a seasonal pricing rule for a room type.
 * Pure Java — no JPA, no framework.
 */
public class Tariff {

    public enum Season { low, mid, high, peak }

    private Long id;
    private String name;
    private Season season;
    private RoomType roomType;          // nullable → applies to all types
    private BigDecimal pricePerNight;
    private BigDecimal discountPercent; // 0-100
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Tariff() {}

    private Tariff(Builder b) {
        this.id              = b.id;
        this.name            = b.name;
        this.season          = b.season;
        this.roomType        = b.roomType;
        this.pricePerNight   = b.pricePerNight;
        this.discountPercent = b.discountPercent;
        this.startDate       = b.startDate;
        this.endDate         = b.endDate;
        this.active          = b.active;
        this.createdAt       = b.createdAt;
        this.updatedAt       = b.updatedAt;
    }

    // ─── Factory ───────────────────────────────────────────────────

    public static Tariff create(String name, Season season, RoomType roomType,
                                BigDecimal pricePerNight, BigDecimal discountPercent,
                                LocalDate startDate, LocalDate endDate) {
        validateName(name);
        validatePrice(pricePerNight);
        validateDates(startDate, endDate);
        validateDiscount(discountPercent);

        return new Builder()
                .name(name)
                .season(season)
                .roomType(roomType)
                .pricePerNight(pricePerNight)
                .discountPercent(discountPercent)
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Tariff reconstitute(Long id, String name, Season season, RoomType roomType,
                                      BigDecimal pricePerNight, BigDecimal discountPercent,
                                      LocalDate startDate, LocalDate endDate,
                                      boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Builder()
                .id(id).name(name).season(season).roomType(roomType)
                .pricePerNight(pricePerNight).discountPercent(discountPercent)
                .startDate(startDate).endDate(endDate)
                .active(active).createdAt(createdAt).updatedAt(updatedAt)
                .build();
    }

    // ─── Domain Behaviours ─────────────────────────────────────────

    public void update(String name, Season season, RoomType roomType,
                       BigDecimal pricePerNight, BigDecimal discountPercent,
                       LocalDate startDate, LocalDate endDate, boolean active) {
        validateName(name);
        validatePrice(pricePerNight);
        validateDates(startDate, endDate);
        validateDiscount(discountPercent);
        this.name            = name;
        this.season          = season;
        this.roomType        = roomType;
        this.pricePerNight   = pricePerNight;
        this.discountPercent = discountPercent;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.active          = active;
        this.updatedAt       = LocalDateTime.now();
    }

    public void applyDiscount(BigDecimal discount) {
        validateDiscount(discount);
        this.discountPercent = discount;
        this.updatedAt       = LocalDateTime.now();
    }

    /** Effective price after discount */
    public BigDecimal effectivePrice() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return pricePerNight;
        }
        BigDecimal factor = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100)));
        return pricePerNight.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // ─── Invariants ────────────────────────────────────────────────

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Le nom du tarif est requis");
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix doit être supérieur à 0");
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Les dates de début et fin sont requises");
        if (!end.isAfter(start))
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
    }

    private static void validateDiscount(BigDecimal discount) {
        if (discount != null &&
            (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0))
            throw new IllegalArgumentException("La remise doit être entre 0 et 100");
    }

    // ─── Getters ───────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public String getName()              { return name; }
    public Season getSeason()            { return season; }
    public RoomType getRoomType()        { return roomType; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public BigDecimal getDiscountPercent(){ return discountPercent; }
    public LocalDate getStartDate()      { return startDate; }
    public LocalDate getEndDate()        { return endDate; }
    public boolean isActive()            { return active; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    // ─── Builder ───────────────────────────────────────────────────

    public static final class Builder {
        private Long id;
        private String name;
        private Season season = Season.mid;
        private RoomType roomType;
        private BigDecimal pricePerNight;
        private BigDecimal discountPercent = BigDecimal.ZERO;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                          { this.id = id; return this; }
        public Builder name(String name)                     { this.name = name; return this; }
        public Builder season(Season season)                 { this.season = season; return this; }
        public Builder roomType(RoomType roomType)           { this.roomType = roomType; return this; }
        public Builder pricePerNight(BigDecimal p)           { this.pricePerNight = p; return this; }
        public Builder discountPercent(BigDecimal d)         { this.discountPercent = d; return this; }
        public Builder startDate(LocalDate d)                { this.startDate = d; return this; }
        public Builder endDate(LocalDate d)                  { this.endDate = d; return this; }
        public Builder active(boolean a)                     { this.active = a; return this; }
        public Builder createdAt(LocalDateTime dt)           { this.createdAt = dt; return this; }
        public Builder updatedAt(LocalDateTime dt)           { this.updatedAt = dt; return this; }
        public Tariff build()                                { return new Tariff(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tariff t)) return false;
        return Objects.equals(id, t.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Tariff{id=" + id + ", name='" + name + "', season=" + season + ", price=" + pricePerNight + "}";
    }
}
package com.hotel.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DOMAIN LAYER — Room Aggregate Root
 * Pure Java object — no JPA, no Spring, no framework.
 *
 * FIX : ajout du champ amenities (List<String>) pour stocker les équipements
 *       de la chambre (WiFi, TV, Jacuzzi, etc.).
 *
 * FIX : validatePrice accepte désormais null — le prix de base est géré
 *       via la section "Prix de base par type" dans AdminTariffs.
 *       Si price est fourni, il doit rester > 0.
 */
public class Room {

    private Long         id;
    private String       number;
    private RoomType     type;
    private BigDecimal   price;
    private int          capacity;
    private RoomStatus   status;
    private String       description;
    private List<String> amenities;

    protected Room() {}

    private Room(Builder builder) {
        this.id          = builder.id;
        this.number      = builder.number;
        this.type        = builder.type;
        this.price       = builder.price;
        this.capacity    = builder.capacity;
        this.status      = builder.status;
        this.description = builder.description;
        this.amenities   = builder.amenities != null ? new ArrayList<>(builder.amenities) : new ArrayList<>();
    }

    // ────────────────── Factory Methods ──────────────────

    public static Room create(String number, RoomType type, BigDecimal price,
                              int capacity, String description) {
        return create(number, type, price, capacity, description, null);
    }

    /** FIX : variante avec amenities */
    public static Room create(String number, RoomType type, BigDecimal price,
                              int capacity, String description, List<String> amenities) {
        validateNumber(number);
        validateType(type);
        validatePrice(price);   // price peut être null — voir validatePrice ci-dessous
        validateCapacity(capacity);

        return new Builder()
                .number(number.trim())
                .type(type)
                .price(price)
                .capacity(capacity)
                .status(RoomStatus.AVAILABLE)
                .description(description != null ? description.trim() : null)
                .amenities(amenities)
                .build();
    }

    public static Room reconstitute(Long id, String number, RoomType type,
                                    BigDecimal price, int capacity,
                                    RoomStatus status, String description) {
        return reconstitute(id, number, type, price, capacity, status, description, null);
    }

    /** FIX : variante avec amenities */
    public static Room reconstitute(Long id, String number, RoomType type,
                                    BigDecimal price, int capacity,
                                    RoomStatus status, String description,
                                    List<String> amenities) {
        return new Builder()
                .id(id)
                .number(number)
                .type(type)
                .price(price)
                .capacity(capacity)
                .status(status)
                .description(description)
                .amenities(amenities)
                .build();
    }

    // ────────────────── Domain Behaviours ──────────────────

    public void updateDetails(String number, RoomType type, BigDecimal price,
                              int capacity, String description) {
        updateDetails(number, type, price, capacity, description, this.amenities);
    }

    /** FIX : variante avec amenities */
    public void updateDetails(String number, RoomType type, BigDecimal price,
                              int capacity, String description, List<String> amenities) {
        validateNumber(number);
        validateType(type);
        validatePrice(price);   // price peut être null
        validateCapacity(capacity);

        this.number      = number.trim();
        this.type        = type;
        this.price       = price;
        this.capacity    = capacity;
        this.description = description != null ? description.trim() : null;
        this.amenities   = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
    }

    public void markAsOccupied() {
        if (this.status == RoomStatus.OCCUPIED) {
            throw new IllegalStateException("La chambre est déjà occupée");
        }
        this.status = RoomStatus.OCCUPIED;
    }

    public void markAsAvailable() {
        this.status = RoomStatus.AVAILABLE;
    }

    public void markAsMaintenance() {
        this.status = RoomStatus.MAINTENANCE;
    }

    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }

    // ────────────────── Domain Invariants ──────────────────

    private static void validateNumber(String number) {
        if (number == null || number.isBlank())
            throw new IllegalArgumentException("Le numéro de chambre ne peut pas être vide");
        if (number.trim().length() > 10)
            throw new IllegalArgumentException("Le numéro de chambre ne peut pas dépasser 10 caractères");
    }

    private static void validateType(RoomType type) {
        if (type == null)
            throw new IllegalArgumentException("Le type de chambre est obligatoire");
    }

    /**
     * FIX : price est maintenant optionnel (null autorisé).
     * Le prix de base est défini via la section "Prix de base par type" (AdminTariffs).
     * Si un prix est fourni, il doit rester strictement positif.
     */
    private static void validatePrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix doit être supérieur à 0");
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("La capacité doit être supérieure à 0");
        if (capacity > 20)
            throw new IllegalArgumentException("La capacité ne peut pas dépasser 20 personnes");
    }

    // ────────────────── Getters ──────────────────

    public Long         getId()          { return id; }
    public String       getNumber()      { return number; }
    public RoomType     getType()        { return type; }
    public BigDecimal   getPrice()       { return price; }
    public int          getCapacity()    { return capacity; }
    public RoomStatus   getStatus()      { return status; }
    public String       getDescription() { return description; }
    public List<String> getAmenities()   { return Collections.unmodifiableList(amenities); }

    // ────────────────── Builder ──────────────────

    public static final class Builder {
        private Long         id;
        private String       number;
        private RoomType     type;
        private BigDecimal   price;
        private int          capacity;
        private RoomStatus   status;
        private String       description;
        private List<String> amenities;

        public Builder id(Long id)                  { this.id = id; return this; }
        public Builder number(String v)              { this.number = v; return this; }
        public Builder type(RoomType v)              { this.type = v; return this; }
        public Builder price(BigDecimal v)           { this.price = v; return this; }
        public Builder capacity(int v)               { this.capacity = v; return this; }
        public Builder status(RoomStatus v)          { this.status = v; return this; }
        public Builder description(String v)         { this.description = v; return this; }
        public Builder amenities(List<String> v)     { this.amenities = v; return this; }

        public Room build() { return new Room(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room r)) return false;
        return Objects.equals(number, r.number);
    }

    @Override public int hashCode() { return Objects.hash(number); }

    @Override
    public String toString() {
        return "Room{id=" + id + ", number='" + number + "', type=" + type + ", status=" + status + "}";
    }
}
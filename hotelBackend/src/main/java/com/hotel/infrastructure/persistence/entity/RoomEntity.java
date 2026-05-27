package com.hotel.infrastructure.persistence.entity;

import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Room.
 *
 * FIX : ajout du champ amenities stocké comme une collection d'éléments
 *       dans une table séparée (room_amenities).
 *       @ElementCollection + @CollectionTable est la solution JPA standard
 *       pour stocker une List<String> sans entité dédiée.
 */
@Entity
@Table(
    name = "rooms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rooms_number", columnNames = "number")
    }
)
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(length = 500)
    private String description;

    /**
     * FIX : liste des équipements stockée dans la table room_amenities.
     * Chaque ligne = (room_id, amenity).
     * Hibernate crée automatiquement cette table au démarrage (ddl-auto=update).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "room_amenities",
        joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "amenity", length = 100)
    private List<String> amenities = new ArrayList<>();

    public RoomEntity() {}

    // ────────────────── Getters / Setters ──────────────────

    public Long        getId()                        { return id; }
    public void        setId(Long v)                  { this.id = v; }

    public String      getNumber()                    { return number; }
    public void        setNumber(String v)            { this.number = v; }

    public RoomType    getType()                      { return type; }
    public void        setType(RoomType v)            { this.type = v; }

    public BigDecimal  getPrice()                     { return price; }
    public void        setPrice(BigDecimal v)         { this.price = v; }

    public int         getCapacity()                  { return capacity; }
    public void        setCapacity(int v)             { this.capacity = v; }

    public RoomStatus  getStatus()                    { return status; }
    public void        setStatus(RoomStatus v)        { this.status = v; }

    public String      getDescription()               { return description; }
    public void        setDescription(String v)       { this.description = v; }

    public List<String> getAmenities()                { return amenities; }
    public void        setAmenities(List<String> v)   { this.amenities = v != null ? v : new ArrayList<>(); }

    // ────────────────── Builder ──────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long        id;
        private String      number;
        private RoomType    type;
        private BigDecimal  price;
        private int         capacity;
        private RoomStatus  status;
        private String      description;
        private List<String> amenities = new ArrayList<>();

        public Builder id(Long v)               { this.id = v; return this; }
        public Builder number(String v)          { this.number = v; return this; }
        public Builder type(RoomType v)          { this.type = v; return this; }
        public Builder price(BigDecimal v)       { this.price = v; return this; }
        public Builder capacity(int v)           { this.capacity = v; return this; }
        public Builder status(RoomStatus v)      { this.status = v; return this; }
        public Builder description(String v)     { this.description = v; return this; }
        public Builder amenities(List<String> v) { this.amenities = v != null ? v : new ArrayList<>(); return this; }

        public RoomEntity build() {
            RoomEntity e = new RoomEntity();
            e.id          = id;
            e.number      = number;
            e.type        = type;
            e.price       = price;
            e.capacity    = capacity;
            e.status      = status;
            e.description = description;
            e.amenities   = amenities;
            return e;
        }
    }
}
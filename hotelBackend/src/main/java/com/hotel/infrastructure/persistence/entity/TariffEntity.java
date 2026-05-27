package com.hotel.infrastructure.persistence.entity;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff.Season;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Tariff.
 * Completely isolated from the domain model.
 */
@Entity
@Table(name = "tariffs")
public class TariffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Season season;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 20)
    private RoomType roomType;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TariffEntity() {}

    // ────────────────── Getters / Setters ──────────────────

    public Long          getId()                         { return id; }
    public void          setId(Long v)                   { this.id = v; }

    public String        getName()                       { return name; }
    public void          setName(String v)               { this.name = v; }

    public Season        getSeason()                     { return season; }
    public void          setSeason(Season v)             { this.season = v; }

    public RoomType      getRoomType()                   { return roomType; }
    public void          setRoomType(RoomType v)         { this.roomType = v; }

    public BigDecimal    getPricePerNight()              { return pricePerNight; }
    public void          setPricePerNight(BigDecimal v)  { this.pricePerNight = v; }

    public BigDecimal    getDiscountPercent()            { return discountPercent; }
    public void          setDiscountPercent(BigDecimal v){ this.discountPercent = v; }

    public LocalDate     getStartDate()                  { return startDate; }
    public void          setStartDate(LocalDate v)       { this.startDate = v; }

    public LocalDate     getEndDate()                    { return endDate; }
    public void          setEndDate(LocalDate v)         { this.endDate = v; }

    public boolean       isActive()                      { return active; }
    public void          setActive(boolean v)            { this.active = v; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}
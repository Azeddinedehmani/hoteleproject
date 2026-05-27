package com.hotel.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Equipment.
 * Completely isolated from the domain model.
 */
@Entity
@Table(name = "equipment")
public class EquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EquipmentEntity() {}

    // ────────────────── Getters / Setters ──────────────────

    public Long          getId()                         { return id; }
    public void          setId(Long v)                   { this.id = v; }

    public String        getName()                       { return name; }
    public void          setName(String v)               { this.name = v; }

    public String        getCategory()                   { return category; }
    public void          setCategory(String v)           { this.category = v; }

    public String        getDescription()                { return description; }
    public void          setDescription(String v)        { this.description = v; }

    public String        getIcon()                       { return icon; }
    public void          setIcon(String v)               { this.icon = v; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}
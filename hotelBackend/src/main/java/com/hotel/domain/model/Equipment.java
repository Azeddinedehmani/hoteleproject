package com.hotel.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DOMAIN LAYER — Equipment Aggregate Root.
 * Represents a hotel amenity/equipment item (TV, Jacuzzi, AC, etc.).
 * Pure Java — no JPA, no framework.
 */
public class Equipment {

    private Long id;
    private String name;
    private String category;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Equipment() {}

    private Equipment(Builder b) {
        this.id          = b.id;
        this.name        = b.name;
        this.category    = b.category;
        this.description = b.description;
        this.icon        = b.icon;
        this.createdAt   = b.createdAt;
        this.updatedAt   = b.updatedAt;
    }

    // ─── Factory ───────────────────────────────────────────────────

    public static Equipment create(String name, String category, String description, String icon) {
        validateName(name);
        return new Builder()
                .name(name)
                .category(category)
                .description(description)
                .icon(icon)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Equipment reconstitute(Long id, String name, String category,
                                         String description, String icon,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Builder()
                .id(id).name(name).category(category)
                .description(description).icon(icon)
                .createdAt(createdAt).updatedAt(updatedAt)
                .build();
    }

    // ─── Domain Behaviours ─────────────────────────────────────────

    public void update(String name, String category, String description, String icon) {
        validateName(name);
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.icon        = icon;
        this.updatedAt   = LocalDateTime.now();
    }

    // ─── Invariants ────────────────────────────────────────────────

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Le nom de l'équipement est requis");
        if (name.trim().length() > 100)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 100 caractères");
    }

    // ─── Getters ───────────────────────────────────────────────────

    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getCategory()      { return category; }
    public String getDescription()   { return description; }
    public String getIcon()          { return icon; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Builder ───────────────────────────────────────────────────

    public static final class Builder {
        private Long id;
        private String name;
        private String category;
        private String description;
        private String icon;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                       { this.id = id; return this; }
        public Builder name(String name)                  { this.name = name; return this; }
        public Builder category(String category)          { this.category = category; return this; }
        public Builder description(String description)    { this.description = description; return this; }
        public Builder icon(String icon)                  { this.icon = icon; return this; }
        public Builder createdAt(LocalDateTime dt)        { this.createdAt = dt; return this; }
        public Builder updatedAt(LocalDateTime dt)        { this.updatedAt = dt; return this; }
        public Equipment build()                          { return new Equipment(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipment e)) return false;
        return Objects.equals(id, e.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Equipment{id=" + id + ", name='" + name + "', category='" + category + "'}";
    }
}
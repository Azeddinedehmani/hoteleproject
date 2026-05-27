package com.hotel.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DOMAIN LAYER — User Aggregate Root
 * Pure Java object — no JPA, no Spring, no framework.
 * This is the heart of the domain.
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ────────────────────── Constructors ──────────────────────

    protected User() {}

    private User(Builder builder) {
        this.id        = builder.id;
        this.name      = builder.name;
        this.email     = builder.email;
        this.password  = builder.password;
        this.role      = builder.role;
        this.active    = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ────────────────────── Factory Methods ──────────────────────

    /**
     * Creates a new user (registration flow).
     * Enforces domain invariants.
     */
    public static User create(String name, String email, String encodedPassword, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(encodedPassword);
        validateRole(role);

        return new Builder()
                .name(name)
                .email(email.toLowerCase().trim())
                .password(encodedPassword)
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Reconstitute a User from persistence (no validation — data already validated).
     */
    public static User reconstitute(Long id, String name, String email,
                                     String password, Role role, boolean active,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Builder()
                .id(id)
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .active(active)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ────────────────────── Domain Behaviours ──────────────────────

    public void changeRole(Role newRole) {
        validateRole(newRole);
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String name, String email) {
        validateName(name);
        validateEmail(email);
        this.name = name;
        this.email = email.toLowerCase().trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        validatePassword(encodedPassword);
        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isReceptionniste() {
        return Role.RECEPTIONNISTE.equals(this.role);
    }

    public boolean isClient() {
        return Role.CLIENT.equals(this.role);
    }

    // ────────────────────── Domain Invariants ──────────────────────

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Le nom doit contenir entre 2 et 100 caractères");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
    }

    private static void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Le rôle ne peut pas être null");
        }
    }

    // ────────────────────── Getters ──────────────────────

    public Long getId()               { return id; }
    public String getName()           { return name; }
    public String getEmail()          { return email; }
    public String getPassword()       { return password; }
    public Role getRole()             { return role; }
    public boolean isActive()         { return active; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }

    // ────────────────────── Builder ──────────────────────

    public static final class Builder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role = Role.CLIENT;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                       { this.id = id; return this; }
        public Builder name(String name)                  { this.name = name; return this; }
        public Builder email(String email)                { this.email = email; return this; }
        public Builder password(String password)          { this.password = password; return this; }
        public Builder role(Role role)                    { this.role = role; return this; }
        public Builder active(boolean active)             { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() { return new User(this); }
    }

    // ────────────────────── equals / hashCode ──────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + '}';
    }
}
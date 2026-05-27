package com.hotel.domain.model;

import java.util.Objects;

/**
 * DOMAIN LAYER — Client Aggregate Root
 * Pure Java object — no JPA, no Spring, no framework.
 */
public class Client {

    private Long   id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String cin;

    // ────────────────── Constructors ──────────────────

    protected Client() {}

    private Client(Builder builder) {
        this.id        = builder.id;
        this.firstName = builder.firstName;
        this.lastName  = builder.lastName;
        this.email     = builder.email;
        this.phone     = builder.phone;
        this.cin       = builder.cin;
    }

    // ────────────────── Factory Methods ──────────────────

    /**
     * Creates a new Client (creation flow).
     * Enforces domain invariants.
     */
    public static Client create(String firstName, String lastName,
                                String email, String phone, String cin) {
        validateFirstName(firstName);
        validateLastName(lastName);
        validateEmail(email);

        return new Builder()
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .email(email.toLowerCase().trim())
                .phone(phone != null ? phone.trim() : null)
                .cin(cin != null ? cin.trim().toUpperCase() : null)
                .build();
    }

    /**
     * Reconstitute a Client from persistence (no validation — data already validated).
     */
    public static Client reconstitute(Long id, String firstName, String lastName,
                                      String email, String phone, String cin) {
        return new Builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .cin(cin)
                .build();
    }

    // ────────────────── Domain Behaviours ──────────────────

    public void updateProfile(String firstName, String lastName,
                              String email, String phone, String cin) {
        validateFirstName(firstName);
        validateLastName(lastName);
        validateEmail(email);

        this.firstName = firstName.trim();
        this.lastName  = lastName.trim();
        this.email     = email.toLowerCase().trim();
        this.phone     = phone != null ? phone.trim() : null;
        this.cin       = cin != null ? cin.trim().toUpperCase() : null;
    }

    /** Convenience: full name */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ────────────────── Domain Invariants ──────────────────

    private static void validateFirstName(String firstName) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("Le prénom ne peut pas être vide");
        if (firstName.trim().length() < 2 || firstName.trim().length() > 60)
            throw new IllegalArgumentException("Le prénom doit contenir entre 2 et 60 caractères");
    }

    private static void validateLastName(String lastName) {
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (lastName.trim().length() < 2 || lastName.trim().length() > 60)
            throw new IllegalArgumentException("Le nom doit contenir entre 2 et 60 caractères");
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        if (!email.contains("@"))
            throw new IllegalArgumentException("Format d'email invalide");
    }

    // ────────────────── Getters ──────────────────

    public Long   getId()        { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }
    public String getPhone()     { return phone; }
    public String getCin()       { return cin; }

    // ────────────────── Builder ──────────────────

    public static final class Builder {
        private Long   id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String cin;

        public Builder id(Long id)               { this.id = id; return this; }
        public Builder firstName(String v)        { this.firstName = v; return this; }
        public Builder lastName(String v)         { this.lastName = v; return this; }
        public Builder email(String v)            { this.email = v; return this; }
        public Builder phone(String v)            { this.phone = v; return this; }
        public Builder cin(String v)              { this.cin = v; return this; }

        public Client build() { return new Client(this); }
    }

    // ────────────────── equals / hashCode ──────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client c)) return false;
        return Objects.equals(email, c.email);
    }

    @Override
    public int hashCode() { return Objects.hash(email); }

    @Override
    public String toString() {
        return "Client{id=" + id + ", email='" + email + "', cin='" + cin + "'}";
    }
}
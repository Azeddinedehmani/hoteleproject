package com.hotel.infrastructure.persistence.entity;

import jakarta.persistence.*;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for Client.
 */
@Entity
@Table(
    name = "clients",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_clients_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_clients_cin",   columnNames = "cin")
    }
)
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String firstName;

    @Column(nullable = false, length = 60)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 20, unique = true)
    private String cin;

    // ────────────────── Constructors ──────────────────

    public ClientEntity() {}

    private ClientEntity(Builder builder) {
        this.id        = builder.id;
        this.firstName = builder.firstName;
        this.lastName  = builder.lastName;
        this.email     = builder.email;
        this.phone     = builder.phone;
        this.cin       = builder.cin;
    }

    // ────────────────── Builder ──────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long   id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String cin;

        public Builder id(Long id)             { this.id = id; return this; }
        public Builder firstName(String v)      { this.firstName = v; return this; }
        public Builder lastName(String v)       { this.lastName = v; return this; }
        public Builder email(String v)          { this.email = v; return this; }
        public Builder phone(String v)          { this.phone = v; return this; }
        public Builder cin(String v)            { this.cin = v; return this; }

        public ClientEntity build() { return new ClientEntity(this); }
    }

    // ────────────────── Getters / Setters ──────────────────

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getFirstName()                 { return firstName; }
    public void setFirstName(String firstName)   { this.firstName = firstName; }

    public String getLastName()                  { return lastName; }
    public void setLastName(String lastName)     { this.lastName = lastName; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public String getPhone()             { return phone; }
    public void setPhone(String phone)   { this.phone = phone; }

    public String getCin()               { return cin; }
    public void setCin(String cin)       { this.cin = cin; }
}
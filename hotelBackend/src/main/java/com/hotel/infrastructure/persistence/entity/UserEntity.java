package com.hotel.infrastructure.persistence.entity;

import com.hotel.domain.model.Role;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * INFRASTRUCTURE LAYER — JPA Entity for User.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ────────────────── Constructors ──────────────────

    public UserEntity() {}

    private UserEntity(Builder builder) {
        this.id        = builder.id;
        this.name      = builder.name;
        this.email     = builder.email;
        this.password  = builder.password;
        this.role      = builder.role;
        this.active    = builder.active;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ────────────────── Builder ──────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Role role;
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

        public UserEntity build() { return new UserEntity(this); }
    }

    // ────────────────── Getters / Setters ──────────────────

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword()               { return password; }
    public void setPassword(String password)  { this.password = password; }

    public Role getRole()              { return role; }
    public void setRole(Role role)     { this.role = role; }

    public boolean isActive()          { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
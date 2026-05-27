package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.User;
import com.hotel.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain User and JPA UserEntity.
 * Keeps domain and persistence completely decoupled.
 */
@Component
public class UserEntityMapper {

    /**
     * Converts a JPA entity back to a domain model (reconstitution from DB).
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converts a domain model to a JPA entity for persistence.
     */
    public UserEntity toEntity(User user) {
        if (user == null) return null;
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Role;
import com.hotel.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA Repository.
 * This is the actual Spring/JPA dependency.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRole(Role role);

    boolean existsByEmail(String email);
}
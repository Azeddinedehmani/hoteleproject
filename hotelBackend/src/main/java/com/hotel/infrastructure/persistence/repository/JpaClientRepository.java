package com.hotel.infrastructure.persistence.repository;

import com.hotel.infrastructure.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for ClientEntity.
 * Only used by ClientRepositoryAdapter. Never injected directly into the domain or application layer.
 */
public interface JpaClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByEmail(String email);

    Optional<ClientEntity> findByCin(String cin);

    boolean existsByEmail(String email);

    boolean existsByCin(String cin);
}
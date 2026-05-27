package com.hotel.infrastructure.persistence.repository;

import com.hotel.infrastructure.persistence.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for EquipmentEntity.
 * Only used by EquipmentRepositoryAdapter.
 */
public interface JpaEquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

    List<EquipmentEntity> findByCategory(String category);

    boolean existsByName(String name);
}
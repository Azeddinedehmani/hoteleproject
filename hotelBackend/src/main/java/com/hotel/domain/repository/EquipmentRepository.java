package com.hotel.domain.repository;

import com.hotel.domain.model.Equipment;

import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — Equipment Repository Port.
 */
public interface EquipmentRepository {

    Equipment save(Equipment equipment);

    Optional<Equipment> findById(Long id);

    List<Equipment> findAll();

    List<Equipment> findByCategory(String category);

    boolean existsByName(String name);

    void deleteById(Long id);

    long count();
}
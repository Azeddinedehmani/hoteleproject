package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Equipment;
import com.hotel.domain.repository.EquipmentRepository;
import com.hotel.infrastructure.persistence.mapper.EquipmentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's EquipmentRepository port.
 * Bridge between the domain and JPA.
 */
@Component
@RequiredArgsConstructor
public class EquipmentRepositoryAdapter implements EquipmentRepository {

    private final JpaEquipmentRepository jpaEquipmentRepository;
    private final EquipmentEntityMapper  mapper;

    @Override
    public Equipment save(Equipment equipment) {
        var entity = mapper.toEntity(equipment);
        var saved  = jpaEquipmentRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        return jpaEquipmentRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Equipment> findAll() {
        return jpaEquipmentRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Equipment> findByCategory(String category) {
        return jpaEquipmentRepository.findByCategory(category)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaEquipmentRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        jpaEquipmentRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaEquipmentRepository.count();
    }
}
package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Equipment;
import com.hotel.infrastructure.persistence.entity.EquipmentEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain Equipment and JPA EquipmentEntity.
 */
@Component
public class EquipmentEntityMapper {

    /** JPA entity → domain model */
    public Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) return null;
        return Equipment.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getIcon(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /** Domain model → JPA entity */
    public EquipmentEntity toEntity(Equipment equipment) {
        if (equipment == null) return null;
        EquipmentEntity entity = new EquipmentEntity();
        entity.setId(equipment.getId());
        entity.setName(equipment.getName());
        entity.setCategory(equipment.getCategory());
        entity.setDescription(equipment.getDescription());
        entity.setIcon(equipment.getIcon());
        entity.setCreatedAt(equipment.getCreatedAt());
        entity.setUpdatedAt(equipment.getUpdatedAt());
        return entity;
    }
}
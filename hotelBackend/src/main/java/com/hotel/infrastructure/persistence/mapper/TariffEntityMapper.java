package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Tariff;
import com.hotel.infrastructure.persistence.entity.TariffEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain Tariff and JPA TariffEntity.
 */
@Component
public class TariffEntityMapper {

    /** JPA entity → domain model */
    public Tariff toDomain(TariffEntity entity) {
        if (entity == null) return null;
        return Tariff.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getSeason(),
                entity.getRoomType(),
                entity.getPricePerNight(),
                entity.getDiscountPercent(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /** Domain model → JPA entity */
    public TariffEntity toEntity(Tariff tariff) {
        if (tariff == null) return null;
        TariffEntity entity = new TariffEntity();
        entity.setId(tariff.getId());
        entity.setName(tariff.getName());
        entity.setSeason(tariff.getSeason());
        entity.setRoomType(tariff.getRoomType());
        entity.setPricePerNight(tariff.getPricePerNight());
        entity.setDiscountPercent(tariff.getDiscountPercent());
        entity.setStartDate(tariff.getStartDate());
        entity.setEndDate(tariff.getEndDate());
        entity.setActive(tariff.isActive());
        entity.setCreatedAt(tariff.getCreatedAt());
        entity.setUpdatedAt(tariff.getUpdatedAt());
        return entity;
    }
}
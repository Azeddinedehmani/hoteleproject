package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Room;
import com.hotel.infrastructure.persistence.entity.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain Room and JPA RoomEntity.
 *
 * FIX : mapping du champ amenities dans les deux sens.
 */
@Component
public class RoomEntityMapper {

    /** JPA entity → domain model (reconstitution depuis la BDD). */
    public Room toDomain(RoomEntity entity) {
        if (entity == null) return null;
        return Room.reconstitute(
                entity.getId(),
                entity.getNumber(),
                entity.getType(),
                entity.getPrice(),
                entity.getCapacity(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>()
        );
    }

    /** Domain model → JPA entity (pour persistance). */
    public RoomEntity toEntity(Room room) {
        if (room == null) return null;
        return RoomEntity.builder()
                .id(room.getId())
                .number(room.getNumber())
                .type(room.getType())
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .status(room.getStatus())
                .description(room.getDescription())
                .amenities(room.getAmenities() != null ? new ArrayList<>(room.getAmenities()) : new ArrayList<>())
                .build();
    }
}
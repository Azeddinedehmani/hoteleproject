package com.hotel.application.dto.response;

import com.hotel.domain.model.Equipment;

import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Outbound DTO for Equipment data.
 */
public record EquipmentResponse(
        Long id,
        String name,
        String category,
        String description,
        String icon,
        LocalDateTime createdAt
) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getName(),
                equipment.getCategory(),
                equipment.getDescription(),
                equipment.getIcon(),
                equipment.getCreatedAt()
        );
    }
}
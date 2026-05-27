package com.hotel.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * APPLICATION LAYER — Inbound DTO for equipment update (ADMIN).
 */
public record UpdateEquipmentRequest(

        @NotBlank(message = "Le nom de l'équipement est requis")
        @Size(max = 100)
        String name,

        String category,

        @Size(max = 500)
        String description,

        String icon
) {}
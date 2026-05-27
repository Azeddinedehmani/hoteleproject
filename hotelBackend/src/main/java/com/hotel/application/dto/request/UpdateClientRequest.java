package com.hotel.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * APPLICATION LAYER — DTO for PUT /api/clients/{id}
 */
public record UpdateClientRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 60, message = "Le prénom doit contenir entre 2 et 60 caractères")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 60, message = "Le nom doit contenir entre 2 et 60 caractères")
        String lastName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @Pattern(
            regexp = "^(\\+?[0-9\\s\\-().]{6,20})?$",
            message = "Format de téléphone invalide"
        )
        String phone,

        @Size(max = 20, message = "Le CIN ne peut pas dépasser 20 caractères")
        String cin
) {}
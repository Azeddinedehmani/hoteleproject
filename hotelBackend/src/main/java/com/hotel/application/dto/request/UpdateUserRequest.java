package com.hotel.application.dto.request;

import com.hotel.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * APPLICATION LAYER — Inbound DTO for updating user profile.
 */
public record UpdateUserRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 100)
        String name,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        Role role,

        String phone
) {}
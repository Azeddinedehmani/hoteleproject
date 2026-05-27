package com.hotel.application.dto.request;

import com.hotel.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * APPLICATION LAYER — Inbound DTO for admin user creation.
 * Similar to RegisterRequest but used by the admin endpoint (ADMIN role required).
 */
public record CreateUserRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        String phone
) {}
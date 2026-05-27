package com.hotel.application.dto.request;

import com.hotel.domain.model.Role;
import jakarta.validation.constraints.NotNull;

/**
 * APPLICATION LAYER — Inbound DTO for changing a user's role (ADMIN only).
 */
public record UpdateRoleRequest(

        @NotNull(message = "Le rôle est obligatoire")
        Role role
) {}
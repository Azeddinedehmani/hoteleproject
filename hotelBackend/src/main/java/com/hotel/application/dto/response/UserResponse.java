package com.hotel.application.dto.response;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;

import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Outbound DTO for User data.
 * Never exposes the password.
 *
 * FIX : ajout de clientId (nullable) — renseigné lorsque l'utilisateur
 *       possède un enregistrement dans la table clients (rôle CLIENT).
 *       Le frontend BookingPage utilise ce champ pour créer une réservation.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        Long clientId          // ← NOUVEAU : id dans la table clients, null si non-client
) {
    /**
     * Fabrique sans clientId (pour ADMIN / RECEPTIONNISTE ou quand inconnu).
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                null
        );
    }

    /**
     * Fabrique avec clientId (pour CLIENT après login / register).
     */
    public static UserResponse from(User user, Long clientId) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                clientId
        );
    }
}
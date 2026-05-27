package com.hotel.application.dto.request;

import com.hotel.domain.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * APPLICATION LAYER — DTO for POST /api/rooms
 *
 * FIX : price devient nullable — le prix de base est désormais géré
 *       via la section "Prix de base par type" dans AdminTariffs.
 *       Si price est fourni, il doit rester > 0.
 *
 * FIX : capacity passe de int primitif à Integer pour éviter
 *       la désérialisation silencieuse à 0 quand le champ est absent.
 */
public record CreateRoomRequest(

        @NotBlank(message = "Le numéro de chambre est obligatoire")
        @Size(max = 10, message = "Le numéro de chambre ne peut pas dépasser 10 caractères")
        String number,

        @NotNull(message = "Le type de chambre est obligatoire")
        RoomType type,

        // price est optionnel — null autorisé, mais si fourni doit être > 0
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
        @Digits(integer = 8, fraction = 2, message = "Format de prix invalide")
        BigDecimal price,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité doit être au minimum 1")
        @Max(value = 20, message = "La capacité ne peut pas dépasser 20 personnes")
        Integer capacity,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        List<String> amenities
) {}
package com.hotel.application.dto.request;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff.Season;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * APPLICATION LAYER — Inbound DTO for tariff creation (ADMIN).
 */
public record CreateTariffRequest(

        @NotBlank(message = "Le nom du tarif est requis")
        String name,

        @NotNull(message = "La saison est requise")
        Season season,

        RoomType room_type,

        @NotNull(message = "Le prix par nuit est requis")
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
        BigDecimal price_per_night,

        @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
        @DecimalMax(value = "100", message = "La remise ne peut pas dépasser 100%")
        BigDecimal discount_percent,

        @NotNull(message = "La date de début est requise")
        LocalDate start_date,

        @NotNull(message = "La date de fin est requise")
        LocalDate end_date,

        Boolean is_active
) {}
package com.hotel.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * APPLICATION LAYER — Inbound DTO for applying a discount to a tariff (ADMIN).
 */
public record ApplyDiscountRequest(

        @NotNull(message = "La remise est requise")
        @DecimalMin(value = "0", message = "La remise ne peut pas être négative")
        @DecimalMax(value = "100", message = "La remise ne peut pas dépasser 100%")
        BigDecimal discount
) {}
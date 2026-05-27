package com.hotel.application.dto.response;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.model.Tariff.Season;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Outbound DTO for Tariff data.
 */
public record TariffResponse(
        Long id,
        String name,
        Season season,
        String room_type,
        BigDecimal price_per_night,
        BigDecimal discount_percent,
        BigDecimal effective_price,
        LocalDate start_date,
        LocalDate end_date,
        boolean is_active,
        LocalDateTime createdAt
) {
    public static TariffResponse from(Tariff tariff) {
        return new TariffResponse(
                tariff.getId(),
                tariff.getName(),
                tariff.getSeason(),
                tariff.getRoomType() != null ? tariff.getRoomType().name().toLowerCase() : null,
                tariff.getPricePerNight(),
                tariff.getDiscountPercent(),
                tariff.effectivePrice(),
                tariff.getStartDate(),
                tariff.getEndDate(),
                tariff.isActive(),
                tariff.getCreatedAt()
        );
    }
}
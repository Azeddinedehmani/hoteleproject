package com.hotel.application.dto.response;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;

import java.math.BigDecimal;
import java.util.List;

/**
 * APPLICATION LAYER — Outbound DTO for Room data visible to CLIENT role.
 *
 * CORRECTION #1 : Le numéro de chambre (number) est volontairement absent
 * de ce DTO. Les clients voient uniquement le type, la description, le prix
 * et les équipements — jamais le numéro physique de la chambre.
 *
 * Deux alias de prix sont exposés pour compatibilité avec RoomsPage.jsx :
 *   - price          (clé interne)
 *   - price_per_night (clé attendue par le frontend)
 */
public record ClientRoomResponse(
        Long         id,
        RoomType     type,
        BigDecimal   price,
        BigDecimal   price_per_night,
        int          capacity,
        RoomStatus   status,
        String       description,
        List<String> amenities
) {
    public static ClientRoomResponse from(Room room) {
        BigDecimal price = room.getPrice();
        return new ClientRoomResponse(
                room.getId(),
                room.getType(),
                price,
                price,
                room.getCapacity(),
                room.getStatus(),
                room.getDescription(),
                room.getAmenities()
        );
    }
}
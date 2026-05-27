package com.hotel.application.dto.response;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;

import java.math.BigDecimal;
import java.util.List;

/**
 * APPLICATION LAYER — Read-only view of a Room.
 *
 * FIX : ajout du champ amenities dans la réponse.
 *       Le frontend AdminRooms et RoomsPage utilisent ce champ pour afficher
 *       les équipements et pré-cocher les chips lors de la modification.
 *
 *       Deux alias sont exposés pour compatibilité frontend :
 *         - price          (utilisé en interne)
 *         - price_per_night (alias attendu par AdminRooms.jsx et RoomsPage.jsx)
 *
 *       Jackson sérialisera les deux champs dans le JSON.
 */
public record RoomResponse(
        Long         id,
        String       number,
        RoomType     type,
        BigDecimal   price,
        BigDecimal   price_per_night,   // ← alias pour le frontend
        int          capacity,
        RoomStatus   status,
        String       description,
        List<String> amenities          // ← AJOUT
) {
    public static RoomResponse from(Room room) {
        BigDecimal price = room.getPrice();
        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getType(),
                price,
                price,                  // price_per_night = même valeur
                room.getCapacity(),
                room.getStatus(),
                room.getDescription(),
                room.getAmenities()     // ← AJOUT
        );
    }
}
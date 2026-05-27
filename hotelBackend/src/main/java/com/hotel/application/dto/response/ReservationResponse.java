package com.hotel.application.dto.response;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * APPLICATION LAYER — Read-only view of a Reservation  (v3 — appliedPrice + totalPrice)
 *
 * CORRECTION — Ajout de :
 *   - {@code appliedPrice} : prix par nuit calculé par le backend via TariffUseCase
 *   - {@code totalPrice}   : montant total = nights * appliedPrice (recalculé dans enrich())
 */
public record ReservationResponse(
        Long              id,
        Long              clientId,
        Long              roomId,
        LocalDate         checkInDate,
        LocalDate         checkOutDate,
        ReservationStatus status,
        int               guests,
        String            notes,
        long              durationNights,
        LocalDateTime     actualCheckInAt,
        LocalDateTime     actualCheckOutAt,
        ClientResponse    client,
        RoomResponse      room,
        /** Prix par nuit appliqué au moment de la réservation. Null si aucun tarif actif. */
        BigDecimal        appliedPrice,
        /** Montant total estimé = durationNights * appliedPrice. Null si appliedPrice inconnu. */
        BigDecimal        totalPrice
) {
    /** Fabrique simple sans enrichissement (usage interne / tests). */
    public static ReservationResponse from(Reservation r) {
        BigDecimal totalPrice = r.getAppliedPrice() != null
                ? r.getAppliedPrice().multiply(BigDecimal.valueOf(r.getDurationNights()))
                : null;
        return new ReservationResponse(
                r.getId(), r.getClientId(), r.getRoomId(),
                r.getCheckInDate(), r.getCheckOutDate(),
                r.getStatus(), r.getGuests(), r.getNotes(),
                r.getDurationNights(),
                r.getActualCheckInAt(), r.getActualCheckOutAt(),
                null, null,
                r.getAppliedPrice(),
                totalPrice
        );
    }

    /** Fabrique enrichie — appelée par ReservationUseCaseImpl.enrich(). */
    public static ReservationResponse from(Reservation r,
                                           ClientResponse client,
                                           RoomResponse room,
                                           BigDecimal totalPrice) {
        return new ReservationResponse(
                r.getId(), r.getClientId(), r.getRoomId(),
                r.getCheckInDate(), r.getCheckOutDate(),
                r.getStatus(), r.getGuests(), r.getNotes(),
                r.getDurationNights(),
                r.getActualCheckInAt(), r.getActualCheckOutAt(),
                client, room,
                r.getAppliedPrice(),
                totalPrice
        );
    }

    /**
     * Rétrocompatibilité — ancienne signature sans totalPrice.
     * @deprecated Utiliser {@link #from(Reservation, ClientResponse, RoomResponse, BigDecimal)}.
     */
    @Deprecated
    public static ReservationResponse from(Reservation r, ClientResponse client, RoomResponse room) {
        BigDecimal totalPrice = r.getAppliedPrice() != null
                ? r.getAppliedPrice().multiply(BigDecimal.valueOf(r.getDurationNights()))
                : null;
        return from(r, client, room, totalPrice);
    }
}
package com.hotel.application.dto.request;

import com.hotel.domain.model.ReservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * APPLICATION LAYER — Request DTO for updating a Reservation.
 */
public record UpdateReservationRequest(

        @NotNull(message = "La date d'arrivée est obligatoire")
        LocalDate checkInDate,

        @NotNull(message = "La date de départ est obligatoire")
        LocalDate checkOutDate,

        @Min(value = 1, message = "Le nombre de personnes doit être au moins 1")
        int guests,

        String notes,

        /** Optional — if null, status is not changed */
        ReservationStatus status
) {}
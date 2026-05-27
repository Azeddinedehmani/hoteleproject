package com.hotel.application.dto.request;

import com.hotel.domain.model.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * APPLICATION LAYER — DTO de création d'une réservation.
 *
 * POINT 10 — Réservation par type de chambre.
 *
 * Le client transmet un {@code roomType} (ex. DOUBLE, SUITE) ;
 * la chambre précise est attribuée par la réception lors du check-in.
 * {@code roomId} est nullable : obligatoire seulement pour ADMIN / RECEPTIONNISTE.
 *
 * Règle de validation :
 *   - CLIENT   → roomId = null,  roomType = non-null  (contrôlé dans le use-case)
 *   - ADMIN/REC → roomId = non-null, roomType ignoré  (comportement inchangé)
 *
 * POINT 13 — {@code appliedPrice} : total indicatif calculé côté frontend.
 * Nullable, stocké pour traçabilité uniquement.
 */
public record CreateReservationRequest(

        @NotNull(message = "L'identifiant du client est obligatoire")
        Long clientId,

        /**
         * Identifiant de chambre précise — ADMIN / RECEPTIONNISTE uniquement.
         * Null pour les réservations client (le type suffit, POINT 10).
         * @NotNull retiré intentionnellement.
         */
        Long roomId,

        /**
         * POINT 10 — Type de chambre demandé par le client.
         * Obligatoire quand roomId est null.
         */
        RoomType roomType,

        @NotNull(message = "La date d'arrivée est obligatoire")
        LocalDate checkInDate,

        @NotNull(message = "La date de départ est obligatoire")
        LocalDate checkOutDate,

        @Min(value = 1, message = "Le nombre de personnes doit être au moins 1")
        int guests,

        String notes,

        /**
         * POINT 13 — Prix indicatif total présenté au client lors de sa demande.
         * Nullable. Transmis pour traçabilité ; la facturation officielle est
         * calculée par le backend au check-out.
         */
        BigDecimal appliedPrice

) {
    /**
     * Constructeur de compatibilité pour ADMIN / RECEPTIONNISTE
     * qui passent un roomId explicite sans roomType ni appliedPrice.
     */
    public CreateReservationRequest(Long clientId, Long roomId,
                                     LocalDate checkInDate, LocalDate checkOutDate,
                                     int guests, String notes) {
        this(clientId, roomId, null, checkInDate, checkOutDate, guests, notes, null);
    }

    /**
     * Constructeur de compatibilité sans appliedPrice.
     */
    public CreateReservationRequest(Long clientId, Long roomId, RoomType roomType,
                                     LocalDate checkInDate, LocalDate checkOutDate,
                                     int guests, String notes) {
        this(clientId, roomId, roomType, checkInDate, checkOutDate, guests, notes, null);
    }
}
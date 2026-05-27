package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a Reservation cannot be found.
 */
public class ReservationNotFoundException extends DomainException {

    public ReservationNotFoundException(Long id) {
        super("Réservation non trouvée avec l'id : " + id);
    }
}
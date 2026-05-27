package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when an Invoice cannot be found.
 */
public class InvoiceNotFoundException extends DomainException {

    public InvoiceNotFoundException(Long id) {
        super("Facture introuvable — id : " + id);
    }

    public static InvoiceNotFoundException byReservation(Long reservationId) {
        return new InvoiceNotFoundException("Facture introuvable pour la réservation : " + reservationId);
    }

    private InvoiceNotFoundException(String message) {
        super(message);
    }
}
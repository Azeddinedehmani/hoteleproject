package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a reservation business rule is violated.
 * Example: cancellation policy not respected.
 */
public class ReservationException extends DomainException {

    public ReservationException(String message) {
        super(message);
    }
}
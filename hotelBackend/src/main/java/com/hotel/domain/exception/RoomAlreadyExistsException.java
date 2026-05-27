package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a Room with the same number already exists.
 */
public class RoomAlreadyExistsException extends DomainException {

    public RoomAlreadyExistsException(String number) {
        super("Une chambre avec le numéro '" + number + "' existe déjà");
    }
}
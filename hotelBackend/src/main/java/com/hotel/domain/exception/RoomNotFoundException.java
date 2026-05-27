package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a Room cannot be found.
 */
public class RoomNotFoundException extends DomainException {

    public RoomNotFoundException(Long id) {
        super("Chambre non trouvée avec l'id : " + id);
    }

    public RoomNotFoundException(String field, String value) {
        super("Chambre non trouvée avec " + field + " : " + value);
    }
}
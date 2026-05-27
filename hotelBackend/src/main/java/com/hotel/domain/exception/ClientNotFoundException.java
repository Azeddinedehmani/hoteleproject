package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a Client cannot be found by id or email.
 */
public class ClientNotFoundException extends DomainException {

    public ClientNotFoundException(Long id) {
        super("Client non trouvé avec l'id : " + id);
    }

    public ClientNotFoundException(String field, String value) {
        super("Client non trouvé avec " + field + " : " + value);
    }
}
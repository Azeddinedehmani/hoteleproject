package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when an email or CIN is already used by another client.
 */
public class ClientAlreadyExistsException extends DomainException {

    public ClientAlreadyExistsException(String field, String value) {
        super("Un client existe déjà avec " + field + " : " + value);
    }
}
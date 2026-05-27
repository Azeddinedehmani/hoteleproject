package com.hotel.domain.exception;

public class EmailAlreadyExistsException extends DomainException {

    public EmailAlreadyExistsException(String email) {
        super("Un compte existe déjà avec l'email: " + email);
    }
}
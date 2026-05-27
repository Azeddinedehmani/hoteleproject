package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Base domain exception.
 * All business rule violations extend this class.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
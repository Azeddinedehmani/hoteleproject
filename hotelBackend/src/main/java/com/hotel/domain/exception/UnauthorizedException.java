package com.hotel.domain.exception;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Email ou mot de passe incorrect");
    }

    public static UnauthorizedException accountInactive() {
        return new UnauthorizedException("Ce compte est désactivé");
    }
}
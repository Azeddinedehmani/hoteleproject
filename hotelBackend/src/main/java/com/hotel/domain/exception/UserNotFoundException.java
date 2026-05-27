package com.hotel.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super("Utilisateur non trouvé avec l'id: " + id);
    }

    public UserNotFoundException(String email) {
        super("Utilisateur non trouvé avec l'email: " + email);
    }
}
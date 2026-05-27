package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when a Tariff cannot be found.
 *
 * FIXED: added String constructor so TariffUseCaseImpl can call
 *        new TariffNotFoundException("Tarif introuvable: " + id)
 */
public class TariffNotFoundException extends DomainException {

    public TariffNotFoundException(Long id) {
        super("Tarif introuvable — id : " + id);
    }

    public TariffNotFoundException(String message) {
        super(message);
    }
}
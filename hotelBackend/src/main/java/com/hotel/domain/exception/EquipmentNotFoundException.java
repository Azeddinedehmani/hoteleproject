package com.hotel.domain.exception;

/**
 * DOMAIN LAYER — Thrown when an Equipment item cannot be found.
 *
 * FIXED: added String constructor so EquipmentUseCaseImpl can call
 *        new EquipmentNotFoundException("Équipement introuvable: " + id)
 */
public class EquipmentNotFoundException extends DomainException {

    public EquipmentNotFoundException(Long id) {
        super("Équipement introuvable — id : " + id);
    }

    public EquipmentNotFoundException(String message) {
        super(message);
    }
}
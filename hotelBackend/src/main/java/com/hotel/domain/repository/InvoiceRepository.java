package com.hotel.domain.repository;

import com.hotel.domain.model.Invoice;

import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — InvoiceRepository Port
 * Interface définie dans le domaine ; implémentée en infrastructure.
 * Le domaine dépend de cette abstraction, PAS de JPA/Hibernate.
 */
public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    /** Toutes les factures (admin) */
    List<Invoice> findAll();

    /** Facture liée à une réservation donnée */
    Optional<Invoice> findByReservationId(Long reservationId);

    /** Vérifie si une facture existe déjà pour une réservation */
    boolean existsByReservationId(Long reservationId);

    long count();
}
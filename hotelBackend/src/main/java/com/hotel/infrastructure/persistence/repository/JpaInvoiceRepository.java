package com.hotel.infrastructure.persistence.repository;

import com.hotel.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for InvoiceEntity.
 */
public interface JpaInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    Optional<InvoiceEntity> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);
}
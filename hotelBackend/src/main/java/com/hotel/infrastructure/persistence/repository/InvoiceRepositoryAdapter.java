package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Invoice;
import com.hotel.domain.repository.InvoiceRepository;
import com.hotel.infrastructure.persistence.mapper.InvoiceEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing the domain InvoiceRepository port.
 * Bridges the domain with Spring Data JPA.
 */
@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final JpaInvoiceRepository jpaRepo;
    private final InvoiceEntityMapper  mapper;

    public InvoiceRepositoryAdapter(JpaInvoiceRepository jpaRepo,
                                    InvoiceEntityMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper  = mapper;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(invoice)));
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Invoice> findByReservationId(Long reservationId) {
        return jpaRepo.findByReservationId(reservationId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByReservationId(Long reservationId) {
        return jpaRepo.existsByReservationId(reservationId);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Invoice;
import com.hotel.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Mapper between Invoice domain model and InvoiceEntity.
 */
@Component
public class InvoiceEntityMapper {

    public InvoiceEntity toEntity(Invoice invoice) {
        InvoiceEntity e = new InvoiceEntity();
        e.setId(invoice.getId());
        e.setReservationId(invoice.getReservationId());
        e.setNights(invoice.getNights());
        e.setRoomPricePerNight(invoice.getRoomPricePerNight());
        e.setDiscountRate(invoice.getDiscountRate());
        e.setTotalPrice(invoice.getTotalPrice());
        e.setCreatedAt(invoice.getCreatedAt());
        e.setStatus(invoice.getStatus() != null ? invoice.getStatus() : "UNPAID");
        return e;
    }

    public Invoice toDomain(InvoiceEntity e) {
        return Invoice.reconstitute(
                e.getId(),
                e.getReservationId(),
                e.getNights(),
                e.getRoomPricePerNight(),
                e.getDiscountRate(),
                e.getTotalPrice(),
                e.getCreatedAt(),
                e.getStatus() != null ? e.getStatus() : "UNPAID"
        );
    }
}
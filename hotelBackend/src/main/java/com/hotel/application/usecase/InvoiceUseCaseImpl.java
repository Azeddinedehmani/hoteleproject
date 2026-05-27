package com.hotel.application.usecase;

import com.hotel.application.dto.response.InvoiceResponse;
import com.hotel.domain.exception.InvoiceNotFoundException;
import com.hotel.domain.model.Invoice;
import com.hotel.domain.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — InvoiceUseCase Implementation
 */
@Service
@Transactional
public class InvoiceUseCaseImpl implements InvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(InvoiceUseCaseImpl.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceUseCaseImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return InvoiceResponse.from(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByReservationId(Long reservationId) {
        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> InvoiceNotFoundException.byReservation(reservationId));
        return InvoiceResponse.from(invoice);
    }

    // ────────────────── Commands ──────────────────

    @Override
    public InvoiceResponse generateInvoice(Long reservationId,
                                           long nights,
                                           BigDecimal pricePerNight,
                                           BigDecimal discountRate) {
        // Idempotence : ne pas générer une 2e facture pour la même réservation
        if (invoiceRepository.existsByReservationId(reservationId)) {
            log.warn("Facture déjà existante pour reservationId={} — retour de l'existante", reservationId);
            return InvoiceResponse.from(
                    invoiceRepository.findByReservationId(reservationId).orElseThrow()
            );
        }

        Invoice invoice = Invoice.generate(reservationId, nights, pricePerNight, discountRate);
        Invoice saved   = invoiceRepository.save(invoice);

        log.info("Facture générée — id={}, reservationId={}, total={}",
                 saved.getId(), reservationId, saved.getTotalPrice());

        return InvoiceResponse.from(saved);
    }

    @Override
    public InvoiceResponse markAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoice.markAsPaid();   // lève IllegalStateException si déjà PAID

        Invoice saved = invoiceRepository.save(invoice);

        log.info("Facture marquée comme payée — id={}, reservationId={}",
                 saved.getId(), saved.getReservationId());

        return InvoiceResponse.from(saved);
    }
}
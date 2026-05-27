package com.hotel.application.usecase;

import com.hotel.application.dto.response.InvoiceResponse;

import java.util.List;

/**
 * APPLICATION LAYER — Invoice Use Case Port
 */
public interface InvoiceUseCase {

    /** Toutes les factures (ADMIN / RECEPTIONNISTE) */
    List<InvoiceResponse> getAllInvoices();

    /** Facture par ID */
    InvoiceResponse getInvoiceById(Long id);

    /** Facture liée à une réservation */
    InvoiceResponse getInvoiceByReservationId(Long reservationId);

    /**
     * Génère et persiste la facture au moment du check-out.
     *
     * @param reservationId  ID de la réservation
     * @param nights         durée en nuits
     * @param pricePerNight  prix par nuit (snapshot chambre)
     * @param discountRate   remise (0.0 si aucune)
     * @return               la facture créée
     */
    InvoiceResponse generateInvoice(Long reservationId,
                                    long nights,
                                    java.math.BigDecimal pricePerNight,
                                    java.math.BigDecimal discountRate);

    /**
     * Marque une facture comme payée (statut UNPAID → PAID).
     *
     * @param id  ID de la facture à marquer
     * @return    la facture mise à jour
     */
    InvoiceResponse markAsPaid(Long id);
}
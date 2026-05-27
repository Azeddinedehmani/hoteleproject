package com.hotel.presentation.controller;

import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.InvoiceResponse;
import com.hotel.application.usecase.InvoiceUseCase;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.exception.ReservationNotFoundException;
import com.hotel.domain.exception.RoomNotFoundException;
import com.hotel.domain.model.Client;
import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.Room;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.domain.repository.RoomRepository;
import com.hotel.infrastructure.security.config.CustomUserDetails;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PRESENTATION LAYER — Invoice REST Controller.
 */
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUseCase        invoiceUseCase;
    private final ClientRepository      clientRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository        roomRepository;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────── GET ALL ───────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        List<InvoiceResponse> invoices = invoiceUseCase.getAllInvoices()
                .stream()
                .map(this::enrichWithClientId)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    // ─────────────────────────── GET MY INVOICES ───────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getMyInvoices(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String email = userDetails.getUsername();
        var client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email", email));

        List<InvoiceResponse> invoices = invoiceUseCase.getAllInvoices()
                .stream()
                .map(this::enrichWithClientId)
                .filter(inv -> inv.clientId() != null
                        && inv.clientId().equals(client.getId()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    // ─────────────────────────── GET BY ID ───────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(enrichWithClientId(invoiceUseCase.getInvoiceById(id)))
        );
    }

    // ─────────────────────────── PDF DOWNLOAD ───────────────────────────

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        InvoiceResponse invoice     = enrichWithClientId(invoiceUseCase.getInvoiceById(id));
        Client          client      = resolveClient(invoice.reservationId());
        Reservation     reservation = reservationRepository.findById(invoice.reservationId()).orElse(null);

        byte[] pdfBytes = generatePdf(invoice, client, reservation);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"facture-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ─────────────────────────── GET BY RESERVATION ───────────────────────────

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByReservation(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        enrichWithClientId(invoiceUseCase.getInvoiceByReservationId(reservationId))
                )
        );
    }

    // ─────────────────────────── GENERATE ───────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @RequestBody Map<String, Object> body) {

        Object rawId = body.get("reservationId");
        if (rawId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("reservationId est requis"));
        }
        // FIX : Jackson stocke les nombres JSON en Integer dans Map<String,Object>
        Long reservationId = ((Number) rawId).longValue();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));

        long       nights        = reservation.getDurationNights();
        BigDecimal pricePerNight = room.getPrice();
        BigDecimal discountRate  = BigDecimal.ZERO;

        InvoiceResponse invoice = enrichWithClientId(
                invoiceUseCase.generateInvoice(reservationId, nights, pricePerNight, discountRate)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Facture générée avec succès", invoice));
    }

    // ─────────────────────────── MARK AS PAID ───────────────────────────

    /**
     * PATCH /invoices/{id}/pay
     * Marque la facture comme payée (UNPAID → PAID).
     * Accessible par ADMIN et RECEPTIONNISTE uniquement.
     */
    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(@PathVariable Long id) {
        try {
            InvoiceResponse updated = enrichWithClientId(invoiceUseCase.markAsPaid(id));
            return ResponseEntity.ok(ApiResponse.success("Facture marquée comme payée", updated));
        } catch (IllegalStateException ex) {
            // Facture déjà payée
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    // ─────────────────────────── PDF GENERATOR ───────────────────────────

    private byte[] generatePdf(InvoiceResponse inv, Client client, Reservation reservation) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Couleurs
        Color primaryColor = new Color(0x1A, 0x56, 0x76);
        Color lightGray    = new Color(0xF3, 0xF4, 0xF6);
        Color white        = Color.WHITE;
        Color black        = Color.BLACK;
        Color borderGray   = new Color(0xE5, 0xE7, 0xEB);

        // Polices
        Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, white);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, primaryColor);
        Font labelFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, black);
        Font headerFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, white);
        Font bodyFont     = FontFactory.getFont(FontFactory.HELVETICA,       10, black);
        Font totalFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, white);
        Font smallFont    = FontFactory.getFont(FontFactory.HELVETICA,        8, Color.GRAY);

        // ── Bandeau en-tête ──
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(primaryColor);
        headerCell.setPadding(18);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph hotelName = new Paragraph("HÔTEL MANAGEMENT", titleFont);
        hotelName.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(hotelName);

        Font invoiceWhiteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, white);
        Paragraph invoiceLabel = new Paragraph("FACTURE  N°  " + inv.id(), invoiceWhiteFont);
        invoiceLabel.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(invoiceLabel);

        // Statut dans l'en-tête PDF
        String statutLabel = "PAID".equals(inv.status()) ? "✔ PAYÉE" : "⚠ NON PAYÉE";
        Font statutFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11,
                "PAID".equals(inv.status()) ? new Color(0x10, 0xB9, 0x81) : new Color(0xF5, 0x9E, 0x0B));
        Paragraph statutPara = new Paragraph(statutLabel, statutFont);
        statutPara.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(statutPara);

        header.addCell(headerCell);
        doc.add(header);
        doc.add(new Paragraph(" "));

        // ── Bloc informations client ──
        PdfPTable clientTable = new PdfPTable(new float[]{35, 65});
        clientTable.setWidthPercentage(100);
        clientTable.setSpacingAfter(8f);

        PdfPCell clientTitleCell = new PdfPCell(new Phrase("Informations client", subtitleFont));
        clientTitleCell.setColspan(2);
        clientTitleCell.setBorder(Rectangle.NO_BORDER);
        clientTitleCell.setPaddingBottom(6);
        clientTitleCell.setPaddingTop(4);
        clientTable.addCell(clientTitleCell);

        String fullName = (client != null) ? client.getFullName() : "—";
        addInfoRow(clientTable, "Nom complet :", fullName, labelFont, bodyFont, lightGray, borderGray);

        String cin = (client != null && client.getCin() != null && !client.getCin().isBlank())
                ? client.getCin() : "—";
        addInfoRow(clientTable, "CIN :", cin, labelFont, bodyFont, white, borderGray);

        String email = (client != null && client.getEmail() != null) ? client.getEmail() : "—";
        addInfoRow(clientTable, "Email :", email, labelFont, bodyFont, lightGray, borderGray);

        String phone = (client != null && client.getPhone() != null && !client.getPhone().isBlank())
                ? client.getPhone() : "—";
        addInfoRow(clientTable, "Téléphone :", phone, labelFont, bodyFont, white, borderGray);

        doc.add(clientTable);
        doc.add(new Paragraph(" "));

        // ── Bloc informations réservation ──
        PdfPTable resTable = new PdfPTable(new float[]{35, 65});
        resTable.setWidthPercentage(100);
        resTable.setSpacingAfter(8f);

        PdfPCell resTitleCell = new PdfPCell(new Phrase("Réservation", subtitleFont));
        resTitleCell.setColspan(2);
        resTitleCell.setBorder(Rectangle.NO_BORDER);
        resTitleCell.setPaddingBottom(6);
        resTitleCell.setPaddingTop(4);
        resTable.addCell(resTitleCell);

        addInfoRow(resTable, "N° réservation :",
                inv.reservationId() != null ? "#" + inv.reservationId() : "—",
                labelFont, bodyFont, lightGray, borderGray);

        if (reservation != null) {
            addInfoRow(resTable, "Date d'arrivée :",
                    reservation.getCheckInDate() != null
                            ? reservation.getCheckInDate().format(DATE_ONLY) : "—",
                    labelFont, bodyFont, white, borderGray);
            addInfoRow(resTable, "Date de départ :",
                    reservation.getCheckOutDate() != null
                            ? reservation.getCheckOutDate().format(DATE_ONLY) : "—",
                    labelFont, bodyFont, lightGray, borderGray);
        }

        addInfoRow(resTable, "Date d'émission :",
                inv.createdAt() != null ? inv.createdAt().format(DATE_FMT) : "—",
                labelFont, bodyFont, reservation != null ? white : lightGray, borderGray);

        doc.add(resTable);
        doc.add(new Paragraph(" "));

        // ── Titre détail facturation ──
        doc.add(new Paragraph("Détail de la facturation", subtitleFont));
        doc.add(new Paragraph(" "));

        // ── Tableau facturation ──
        PdfPTable table = new PdfPTable(new float[]{50, 25, 25});
        table.setWidthPercentage(100);

        addTableHeader(table, "Description",     primaryColor, headerFont);
        addTableHeader(table, "Quantité / Taux", primaryColor, headerFont);
        addTableHeader(table, "Montant (MAD)",   primaryColor, headerFont);

        BigDecimal pricePerNight = inv.roomPricePerNight() != null
                ? inv.roomPricePerNight() : BigDecimal.ZERO;
        addTableRow(table, "Prix chambre / nuit",
                pricePerNight.setScale(2, RoundingMode.HALF_UP) + " MAD",
                pricePerNight.setScale(2, RoundingMode.HALF_UP) + " MAD",
                lightGray, bodyFont, borderGray);

        BigDecimal nights   = BigDecimal.valueOf(inv.nights());
        BigDecimal subtotal = pricePerNight.multiply(nights);
        addTableRow(table, "Nombre de nuits",
                String.valueOf(inv.nights()),
                subtotal.setScale(2, RoundingMode.HALF_UP) + " MAD",
                white, bodyFont, borderGray);

        BigDecimal discount    = inv.discountRate() != null ? inv.discountRate() : BigDecimal.ZERO;
        BigDecimal discountPct = discount.multiply(BigDecimal.valueOf(100));
        BigDecimal discountAmt = subtotal.multiply(discount);
        addTableRow(table, "Remise appliquée",
                discountPct.setScale(0, RoundingMode.HALF_UP) + " %",
                "- " + discountAmt.setScale(2, RoundingMode.HALF_UP) + " MAD",
                lightGray, bodyFont, borderGray);

        doc.add(table);
        doc.add(new Paragraph(" "));

        // ── Total ──
        BigDecimal total = inv.totalAmount() != null ? inv.totalAmount() : BigDecimal.ZERO;

        PdfPTable totalTable = new PdfPTable(new float[]{70, 30});
        totalTable.setWidthPercentage(100);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
        totalLabelCell.setBackgroundColor(primaryColor);
        totalLabelCell.setPadding(10);
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalLabelCell);

        PdfPCell totalAmtCell = new PdfPCell(
                new Phrase(total.setScale(2, RoundingMode.HALF_UP) + " MAD", totalFont));
        totalAmtCell.setBackgroundColor(primaryColor);
        totalAmtCell.setPadding(10);
        totalAmtCell.setBorder(Rectangle.NO_BORDER);
        totalAmtCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalTable.addCell(totalAmtCell);

        doc.add(totalTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        // ── Pied de page ──
        Paragraph footer = new Paragraph(
                "Ce document est généré automatiquement — Hôtel Management © 2025", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers PDF ──

    private void addInfoRow(PdfPTable table,
                            String label, String value,
                            Font labelFont, Font valueFont,
                            Color bg, Color border) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBackgroundColor(bg);
        lc.setPadding(6);
        lc.setBorderColor(border);
        lc.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBackgroundColor(bg);
        vc.setPadding(6);
        vc.setBorderColor(border);
        vc.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(lc);
        table.addCell(vc);
    }

    private void addTableHeader(PdfPTable table, String text, Color bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table,
                             String label, String qty, String amount,
                             Color bg, Font font, Color border) {
        PdfPCell c1 = new PdfPCell(new Phrase(label,  font));
        PdfPCell c2 = new PdfPCell(new Phrase(qty,    font));
        PdfPCell c3 = new PdfPCell(new Phrase(amount, font));

        for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
            c.setBackgroundColor(bg);
            c.setPadding(7);
            c.setBorderColor(border);
        }
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);
    }

    // ─────────────────────────── PRIVATE HELPERS ───────────────────────────

    private InvoiceResponse enrichWithClientId(InvoiceResponse inv) {
        if (inv == null) return null;
        Long clientId = resolveClientId(inv.reservationId());
        return new InvoiceResponse(
                inv.id(),
                inv.reservationId(),
                clientId,
                inv.nights(),
                inv.roomPricePerNight(),
                inv.discountRate(),
                inv.totalPrice(),
                inv.createdAt(),
                inv.status()
        );
    }

    private Long resolveClientId(Long reservationId) {
        if (reservationId == null) return null;
        return reservationRepository.findById(reservationId)
                .map(Reservation::getClientId)
                .orElse(null);
    }

    private Client resolveClient(Long reservationId) {
        if (reservationId == null) return null;
        return reservationRepository.findById(reservationId)
                .flatMap(r -> clientRepository.findById(r.getClientId()))
                .orElse(null);
    }
}
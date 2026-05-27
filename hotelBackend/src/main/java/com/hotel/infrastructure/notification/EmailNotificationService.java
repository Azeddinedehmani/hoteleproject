package com.hotel.infrastructure.notification;

import com.hotel.domain.model.Client;
import com.hotel.domain.model.Reservation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * INFRASTRUCTURE LAYER — Email Notification Service.
 *
 * FIX PROBLÈME 1 :
 *   1. Propriété {@code email.notifications.enabled} (false par défaut) lue via @Value.
 *      Si désactivée, on logue un WARN et on retourne immédiatement sans tenter d'envoi.
 *      → Le démarrage n'échoue jamais à cause d'un SMTP non configuré.
 *
 *   2. Les méthodes publiques sont annotées @Async → elles s'exécutent dans un thread
 *      séparé et ne bloquent pas le thread de la transaction principale.
 *
 *   3. Tout le corps d'envoi est encadré d'un try/catch large :
 *      une exception JavaMail ne remonte JAMAIS vers le use-case.
 *      Une réservation ou une facture ne doit pas échouer à cause d'un email.
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── FIX PROBLÈME 1 — activation / désactivation sans redémarrage ──────────
    /** Mettre à true en production quand le SMTP est correctement configuré. */
    @Value("${email.notifications.enabled:false}")
    private boolean notificationsEnabled;

    @Value("${spring.mail.from:noreply@hotel.com}")
    private String fromAddress;

    @Value("${hotel.name:Hôtel}")
    private String hotelName;

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Notification envoyée au client lors de la création d'une réservation (statut PENDING).
     *
     * @param client      Le client qui a effectué la réservation
     * @param reservation La réservation créée
     */
    @Async
    public void sendReservationReceived(Client client, Reservation reservation) {
        // FIX PROBLÈME 1 — vérification dès l'entrée de la méthode
        if (!notificationsEnabled) {
            log.warn("[EMAIL DÉSACTIVÉ] sendReservationReceived ignoré (reservationId={}, email={}). "
                    + "Activez email.notifications.enabled=true pour envoyer des emails.",
                    reservation.getId(), maskEmail(client.getEmail()));
            return;
        }

        // FIX PROBLÈME 1 — try/catch large : aucune exception ne sort de cette méthode
        try {
            String subject = "[" + hotelName + "] Votre demande de réservation a été reçue — #"
                    + reservation.getId();
            String body = buildReservationReceivedEmail(client, reservation);
            sendHtmlEmail(client.getEmail(), subject, body);
            log.info("Email 'réservation reçue' envoyé → {} (reservationId={})",
                    maskEmail(client.getEmail()), reservation.getId());
        } catch (Exception e) {
            // Ne jamais propager — la réservation ne doit pas échouer à cause d'un email
            log.error("Échec envoi email 'réservation reçue' (reservationId={}) : {}",
                    reservation.getId(), e.getMessage(), e);
        }
    }

    /**
     * Notification envoyée au client lors de la confirmation d'une réservation (statut CONFIRMED).
     *
     * @param client      Le client concerné
     * @param reservation La réservation confirmée
     */
    @Async
    public void sendReservationConfirmed(Client client, Reservation reservation) {
        // FIX PROBLÈME 1 — vérification dès l'entrée de la méthode
        if (!notificationsEnabled) {
            log.warn("[EMAIL DÉSACTIVÉ] sendReservationConfirmed ignoré (reservationId={}, email={}). "
                    + "Activez email.notifications.enabled=true pour envoyer des emails.",
                    reservation.getId(), maskEmail(client.getEmail()));
            return;
        }

        // FIX PROBLÈME 1 — try/catch large
        try {
            String subject = "[" + hotelName + "] Votre réservation est confirmée — #"
                    + reservation.getId();
            String body = buildReservationConfirmedEmail(client, reservation);
            sendHtmlEmail(client.getEmail(), subject, body);
            log.info("Email 'réservation confirmée' envoyé → {} (reservationId={})",
                    maskEmail(client.getEmail()), reservation.getId());
        } catch (Exception e) {
            log.error("Échec envoi email 'réservation confirmée' (reservationId={}) : {}",
                    reservation.getId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    private String buildReservationReceivedEmail(Client client, Reservation reservation) {
        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">" +
               "<title>Demande reçue</title></head><body style=\"font-family:Arial,sans-serif;" +
               "background:#f9f6f0;margin:0;padding:20px\">" +
               "<div style=\"max-width:600px;margin:auto;background:#fff;border-radius:8px;" +
               "overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)\">" +
               "<div style=\"background:#1a1a2e;padding:30px;text-align:center\">" +
               "<h1 style=\"color:#c9a227;margin:0;font-size:22px\">" + hotelName + "</h1></div>" +
               "<div style=\"padding:30px\">" +
               "<p style=\"color:#333\">Bonjour <strong>" + client.getFirstName() + "</strong>,</p>" +
               "<p style=\"color:#555\">Nous avons bien reçu votre demande de réservation. " +
               "Notre équipe va la traiter dans les meilleurs délais et vous confirmera rapidement.</p>" +
               "<div style=\"background:#f9f6f0;border-left:4px solid #c9a227;padding:16px;margin:20px 0;border-radius:4px\">" +
               "<h3 style=\"color:#1a1a2e;margin:0 0 12px\">Détails de votre demande</h3>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>N° de réservation :</strong> #" + reservation.getId() + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Arrivée :</strong> " + reservation.getCheckInDate().format(DATE_FMT) + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Départ :</strong> " + reservation.getCheckOutDate().format(DATE_FMT) + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Personnes :</strong> " + reservation.getGuests() + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Statut :</strong> " +
               "<span style=\"color:#e67e22;font-weight:bold\">En attente de confirmation</span></p></div>" +
               "<p style=\"color:#555\">Vous recevrez un email dès validation de votre réservation.</p>" +
               "<p style=\"color:#999;font-size:12px;margin-top:30px\">Cet email est envoyé automatiquement, merci de ne pas y répondre.</p></div>" +
               "<div style=\"background:#1a1a2e;padding:16px;text-align:center\">" +
               "<p style=\"color:#888;font-size:12px;margin:0\">&copy; " + hotelName + " — Tous droits réservés</p></div>" +
               "</div></body></html>";
    }

    private String buildReservationConfirmedEmail(Client client, Reservation reservation) {
        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">" +
               "<title>Réservation confirmée</title></head><body style=\"font-family:Arial,sans-serif;" +
               "background:#f9f6f0;margin:0;padding:20px\">" +
               "<div style=\"max-width:600px;margin:auto;background:#fff;border-radius:8px;" +
               "overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)\">" +
               "<div style=\"background:#1a1a2e;padding:30px;text-align:center\">" +
               "<h1 style=\"color:#c9a227;margin:0;font-size:22px\">" + hotelName + "</h1></div>" +
               "<div style=\"padding:30px\">" +
               "<p style=\"color:#333\">Bonjour <strong>" + client.getFirstName() + "</strong>,</p>" +
               "<div style=\"text-align:center;margin:20px 0\">" +
               "<span style=\"font-size:48px\">✓</span>" +
               "<h2 style=\"color:#27ae60;margin:8px 0\">Votre réservation est confirmée !</h2></div>" +
               "<p style=\"color:#555\">Nous avons le plaisir de vous confirmer votre séjour au " + hotelName + ".</p>" +
               "<div style=\"background:#f9f6f0;border-left:4px solid #27ae60;padding:16px;margin:20px 0;border-radius:4px\">" +
               "<h3 style=\"color:#1a1a2e;margin:0 0 12px\">Votre réservation</h3>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>N° de réservation :</strong> #" + reservation.getId() + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Arrivée :</strong> " + reservation.getCheckInDate().format(DATE_FMT) + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Départ :</strong> " + reservation.getCheckOutDate().format(DATE_FMT) + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Durée :</strong> " + reservation.getDurationNights() + " nuit(s)</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Personnes :</strong> " + reservation.getGuests() + "</p>" +
               "<p style=\"margin:4px 0;color:#555\"><strong>Statut :</strong> " +
               "<span style=\"color:#27ae60;font-weight:bold\">Confirmée</span></p></div>" +
               (reservation.getNotes() != null && !reservation.getNotes().isBlank()
                   ? "<p style=\"color:#555\"><strong>Vos remarques :</strong> " + reservation.getNotes() + "</p>"
                   : "") +
               "<p style=\"color:#555\">Nous vous souhaitons un excellent séjour parmi nous.</p>" +
               "<p style=\"color:#999;font-size:12px;margin-top:30px\">Cet email est envoyé automatiquement, merci de ne pas y répondre.</p></div>" +
               "<div style=\"background:#1a1a2e;padding:16px;text-align:center\">" +
               "<p style=\"color:#888;font-size:12px;margin:0\">&copy; " + hotelName + " — Tous droits réservés</p></div>" +
               "</div></body></html>";
    }

    /** Masque partiellement l'email pour les logs (protection des données). */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return "**" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
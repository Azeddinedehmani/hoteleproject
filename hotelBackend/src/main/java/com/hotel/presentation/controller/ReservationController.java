package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateReservationRequest;
import com.hotel.application.dto.request.UpdateReservationRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.ReservationResponse;
import com.hotel.application.usecase.ReservationUseCase;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.exception.UnauthorizedException;
import com.hotel.domain.model.Role;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.infrastructure.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — Contrôleur REST des réservations.
 *
 *  GET    /api/reservations             → toutes les réservations (ADMIN, RECEPTIONNISTE)
 *  GET    /api/reservations/my          → mes réservations (CLIENT)
 *  GET    /api/reservations/{id}        → une réservation (avec vérif propriété CLIENT)
 *  POST   /api/reservations             → créer une réservation
 *  PUT    /api/reservations/{id}        → modifier (ADMIN, RECEPTIONNISTE)
 *  PATCH  /api/reservations/{id}/cancel → annuler (avec vérif propriété CLIENT)
 *  PATCH  /api/reservations/{id}/check-in   → check-in (ADMIN, RECEPTIONNISTE)
 *  PATCH  /api/reservations/{id}/check-out  → check-out (ADMIN, RECEPTIONNISTE)
 *  DELETE /api/reservations/{id}        → supprimer (ADMIN only)
 *
 * POINT 10 — POST /reservations :
 *   - Si le rôle est CLIENT :
 *       • le clientId est résolu depuis le JWT (jamais depuis le payload)
 *       • le roomId du payload est ignoré (le client réserve par type)
 *       • seul le roomType est transmis au use-case
 *   - Si le rôle est ADMIN ou RECEPTIONNISTE :
 *       • le roomId (chambre précise) est respecté si présent
 *       • le roomType est utilisé en fallback si roomId est absent
 *
 * Toute la logique métier reste dans la couche application (use-case).
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;
    private final ClientRepository    clientRepository;

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Long resolveClientId(CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email", email))
                .getId();
    }

    private boolean isClient(CustomUserDetails userDetails) {
        return userDetails != null &&
               userDetails.getDomainUser().getRole() == Role.CLIENT;
    }

    private void assertOwnership(ReservationResponse reservation, Long clientId) {
        Long reservationClientId = reservation.clientId();
        if (reservationClientId == null || !reservationClientId.equals(clientId)) {
            throw new UnauthorizedException(
                    "Vous n'êtes pas autorisé à accéder à cette réservation.");
        }
    }

    // ── GET ALL ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        return ResponseEntity.ok(ApiResponse.success(reservationUseCase.getAllReservations()));
    }

    // ── GET MY ────────────────────────────────────────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long clientId = resolveClientId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(reservationUseCase.getMyReservations(clientId)));
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReservationResponse reservation = reservationUseCase.getReservationById(id);

        if (isClient(userDetails)) {
            Long clientId = resolveClientId(userDetails);
            assertOwnership(reservation, clientId);
        }

        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * POST /api/reservations
     *
     * POINT 10 — Comportement selon le rôle :
     *
     * CLIENT :
     *   - clientId résolu depuis le JWT (sécurité — jamais depuis le payload)
     *   - roomId ignoré (la chambre précise est attribuée au check-in)
     *   - roomType transmis pour la validation de disponibilité (POINT 11)
     *
     * ADMIN / RECEPTIONNISTE :
     *   - clientId du payload respecté
     *   - roomId du payload respecté s'il est non null
     *   - roomType utilisé si roomId est null (attribution flexible)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CreateReservationRequest effectiveRequest = request;

        if (isClient(userDetails)) {
            // POINT 10 : pour les clients, on résout le clientId depuis le JWT
            // et on force roomId à null (la chambre sera attribuée au check-in).
            Long clientId = resolveClientId(userDetails);

            // Conversion du roomType depuis le payload (le frontend envoie le type)
            RoomType roomType = request.roomType();

            effectiveRequest = new CreateReservationRequest(
                    clientId,
                    null,           // roomId → null : la réception attribue au check-in
                    roomType,
                    request.checkInDate(),
                    request.checkOutDate(),
                    request.guests(),
                    request.notes(),
                    request.appliedPrice()
            );
        }

        ReservationResponse created = reservationUseCase.createReservation(effectiveRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Demande de réservation enregistrée avec succès", created));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request) {

        ReservationResponse updated = reservationUseCase.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Réservation mise à jour", updated));
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (isClient(userDetails)) {
            Long clientId = resolveClientId(userDetails);
            ReservationResponse reservation = reservationUseCase.getReservationById(id);
            assertOwnership(reservation, clientId);
        }

        ReservationResponse cancelled = reservationUseCase.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation annulée", cancelled));
    }

    // ── ASSIGN ROOM ───────────────────────────────────────────────────────────

    @PatchMapping("/{id}/assign-room")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> assignRoom(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Long> body) {

        Long roomId = body.get("roomId");
        if (roomId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("roomId est obligatoire"));
        }
        ReservationResponse updated = reservationUseCase.assignRoom(id, roomId);
        return ResponseEntity.ok(ApiResponse.success("Chambre attribuée avec succès", updated));
    }

    // ── CHECK-IN ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkIn(@PathVariable Long id) {
        ReservationResponse updated = reservationUseCase.checkIn(id);
        return ResponseEntity.ok(ApiResponse.success("Check-in effectué", updated));
    }

    // ── CHECK-OUT ─────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkOut(@PathVariable Long id) {
        ReservationResponse updated = reservationUseCase.checkOut(id);
        return ResponseEntity.ok(ApiResponse.success("Check-out effectué", updated));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationUseCase.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation supprimée"));
    }
}
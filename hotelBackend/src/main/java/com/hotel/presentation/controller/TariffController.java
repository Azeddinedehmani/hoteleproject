package com.hotel.presentation.controller;

import com.hotel.application.dto.request.ApplyDiscountRequest;
import com.hotel.application.dto.request.CreateTariffRequest;
import com.hotel.application.dto.request.UpdateTariffRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.TariffResponse;
import com.hotel.application.usecase.TariffUseCase;
import com.hotel.domain.model.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PRESENTATION LAYER — Tariff REST Controller.
 *
 *  GET    /api/tariffs                                              → tous les tarifs (ADMIN, RECEPTIONNISTE)
 *  GET    /api/tariffs/{id}                                         → un tarif       (ADMIN, RECEPTIONNISTE)
 *  GET    /api/tariffs/applicable?roomType=SUITE&checkIn=&checkOut= → tarif applicable (ADMIN, RECEPTIONNISTE, CLIENT)
 *  POST   /api/tariffs                                              → créer un tarif  (ADMIN)
 *  PUT    /api/tariffs/{id}                                         → modifier        (ADMIN)
 *  DELETE /api/tariffs/{id}                                         → supprimer       (ADMIN)
 *  PATCH  /api/tariffs/{id}/discount                                → remise          (ADMIN)
 *
 * CORRECTION #4 : ajout de GET /tariffs/applicable accessible par CLIENT.
 * Permet au frontend BookingPage de récupérer le tarif saisonnier en vigueur
 * pour afficher le prix effectif (après remise) dans le récapitulatif.
 *
 * IMPORTANT : /tariffs/applicable doit être déclaré AVANT /tariffs/{id}
 * pour que Spring ne l'interprète pas comme un path variable.
 */
@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffUseCase tariffUseCase;

    // ──────────────────────────── GET APPLICABLE (CORRECTION #4) ────────────────────────────

    /**
     * GET /api/tariffs/applicable?roomType=SUITE&checkIn=2025-06-01&checkOut=2025-06-05
     *
     * Retourne le tarif actif le mieux correspondant pour la période et le type de chambre.
     * Accessible par CLIENT pour l'affichage du prix dans BookingPage.
     * Retourne 200 avec data=null si aucun tarif n'est applicable (pas d'erreur 404).
     *
     * @param roomType  type de chambre en UPPERCASE (SIMPLE, DOUBLE, SUITE, DELUXE)
     * @param checkIn   date d'arrivée au format YYYY-MM-DD
     * @param checkOut  date de départ au format YYYY-MM-DD
     */
    @GetMapping("/applicable")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<TariffResponse>> getApplicableTariff(
            @RequestParam RoomType roomType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        Optional<TariffResponse> tariff = tariffUseCase.getApplicableTariff(roomType, checkIn, checkOut);
        // Retourner null dans data si aucun tarif — le frontend gère ce cas
        return ResponseEntity.ok(ApiResponse.success(tariff.orElse(null)));
    }

    // ──────────────────────────── GET ALL ────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getAllTariffs() {
        return ResponseEntity.ok(ApiResponse.success(tariffUseCase.getAllTariffs()));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariffById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tariffUseCase.getTariffById(id)));
    }

    // ──────────────────────────── CREATE ────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(
            @Valid @RequestBody CreateTariffRequest request) {

        TariffResponse created = tariffUseCase.createTariff(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tarif créé avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> updateTariff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTariffRequest request) {

        TariffResponse updated = tariffUseCase.updateTariff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tarif mis à jour", updated));
    }

    // ──────────────────────────── APPLY DISCOUNT ────────────────────────────

    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> applyDiscount(
            @PathVariable Long id,
            @Valid @RequestBody ApplyDiscountRequest request) {

        TariffResponse updated = tariffUseCase.applyDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Remise appliquée", updated));
    }

    // ──────────────────────────── DELETE ────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTariff(@PathVariable Long id) {
        tariffUseCase.deleteTariff(id);
        return ResponseEntity.ok(ApiResponse.success("Tarif supprimé avec succès"));
    }
}
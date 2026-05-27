package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateEquipmentRequest;
import com.hotel.application.dto.request.UpdateEquipmentRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.EquipmentResponse;
import com.hotel.application.usecase.EquipmentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — Equipment REST Controller.
 *
 *  GET    /api/equipment          → liste des équipements       (ADMIN, RECEPTIONNISTE)
 *  GET    /api/equipment/{id}     → un équipement par id        (ADMIN, RECEPTIONNISTE)
 *  POST   /api/equipment          → créer un équipement         (ADMIN)
 *  PUT    /api/equipment/{id}     → modifier un équipement      (ADMIN)
 *  DELETE /api/equipment/{id}     → supprimer un équipement     (ADMIN)
 */
@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentUseCase equipmentUseCase;

    // ──────────────────────────── GET ALL ────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getAllEquipment() {
        return ResponseEntity.ok(ApiResponse.success(equipmentUseCase.getAllEquipment()));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(equipmentUseCase.getEquipmentById(id)));
    }

    // ──────────────────────────── CREATE ────────────────────────────

    /**
     * POST /api/equipment
     * Body : { name, category, description, icon }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {

        EquipmentResponse created = equipmentUseCase.createEquipment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Équipement créé avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentRequest request) {

        EquipmentResponse updated = equipmentUseCase.updateEquipment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Équipement mis à jour", updated));
    }

    // ──────────────────────────── DELETE ────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        equipmentUseCase.deleteEquipment(id);
        return ResponseEntity.ok(ApiResponse.success("Équipement supprimé avec succès"));
    }
}
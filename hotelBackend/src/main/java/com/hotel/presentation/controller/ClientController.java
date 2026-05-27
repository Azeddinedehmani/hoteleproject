package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateClientRequest;
import com.hotel.application.dto.request.UpdateClientRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.ClientResponse;
import com.hotel.application.usecase.ClientUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — Client REST Controller.
 * Expose les 5 endpoints attendus par le frontend React.
 *
 *  GET    /api/clients          → liste de tous les clients
 *  GET    /api/clients/{id}     → un client par id
 *  POST   /api/clients          → créer un client
 *  PUT    /api/clients/{id}     → modifier un client
 *  DELETE /api/clients/{id}     → supprimer un client
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientUseCase clientUseCase;

    // ──────────────────────────── GET ALL ────────────────────────────

    /**
     * GET /api/clients
     * Accessible par ADMIN et RECEPTIONNISTE.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAllClients() {
        return ResponseEntity.ok(
                ApiResponse.success(clientUseCase.getAllClients())
        );
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    /**
     * GET /api/clients/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(clientUseCase.getClientById(id))
        );
    }

    // ──────────────────────────── CREATE ────────────────────────────

    /**
     * POST /api/clients
     * Corps : { firstName, lastName, email, phone, cin }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody CreateClientRequest request) {

        ClientResponse created = clientUseCase.createClient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client créé avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    /**
     * PUT /api/clients/{id}
     * Corps : { firstName, lastName, email, phone, cin }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {

        ClientResponse updated = clientUseCase.updateClient(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Client mis à jour avec succès", updated)
        );
    }

    // ──────────────────────────── DELETE ────────────────────────────

    /**
     * DELETE /api/clients/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        clientUseCase.deleteClient(id);
        return ResponseEntity.ok(
                ApiResponse.success("Client supprimé avec succès")
        );
    }
}
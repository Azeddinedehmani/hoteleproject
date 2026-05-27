package com.hotel.presentation.controller;

import com.hotel.application.dto.request.CreateRoomRequest;
import com.hotel.application.dto.request.UpdateRoomRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.ClientRoomResponse;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.application.usecase.RoomUseCase;
import com.hotel.domain.model.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * PRESENTATION LAYER — Room REST Controller.
 *
 *  GET    /api/rooms                           → toutes les chambres (ADMIN, RECEPTIONNISTE)
 *  GET    /api/rooms/{id}                      → une chambre par id  (ADMIN, RECEPTIONNISTE, CLIENT)
 *  GET    /api/rooms/available                 → chambres disponibles (ADMIN, RECEPTIONNISTE, CLIENT)
 *  GET    /api/rooms/available?checkIn=&checkOut= → disponibilité par dates
 *  POST   /api/rooms                           → créer une chambre   (ADMIN)
 *  PUT    /api/rooms/{id}                      → modifier une chambre (ADMIN)
 *  DELETE /api/rooms/{id}                      → supprimer une chambre (ADMIN)
 *
 * CORRECTION #1 : Les routes accessibles par CLIENT retournent un ClientRoomResponse
 * qui n'expose PAS le numéro de chambre (champ number supprimé).
 * Les routes ADMIN/RECEPTIONNISTE continuent de retourner le RoomResponse complet.
 */
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomUseCase roomUseCase;

    // ──────────────────────────── HELPER ────────────────────────────

    private boolean isClient(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
    }

    // ──────────────────────────── GET ALL ────────────────────────────

    /**
     * GET /api/rooms
     * Réservé à l'admin et la réception (liste complète avec tous les statuts).
     * Retourne RoomResponse complet (avec number).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms(
            @RequestParam(required = false) RoomType type) {

        List<RoomResponse> rooms = (type != null)
                ? roomUseCase.getRoomsByType(type)
                : roomUseCase.getAllRooms();

        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    // ──────────────────────────── GET AVAILABLE ────────────────────────────

    /**
     * GET /api/rooms/available
     * GET /api/rooms/available?type=SUITE
     * GET /api/rooms/available?checkIn=2025-06-01&checkOut=2025-06-05
     * GET /api/rooms/available?checkIn=2025-06-01&checkOut=2025-06-05&type=SUITE
     *
     * CORRECTION #1 : si le rôle est CLIENT, on retourne ClientRoomResponse (sans number).
     * ADMIN / RECEPTIONNISTE reçoivent le RoomResponse complet.
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<?>> getAvailableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType type,
            Authentication authentication) {

        List<RoomResponse> rooms;

        if (checkIn != null && checkOut != null && type != null) {
            rooms = roomUseCase.getAvailableRoomsForDatesByType(checkIn, checkOut, type);
        } else if (checkIn != null && checkOut != null) {
            rooms = roomUseCase.getAvailableRoomsForDates(checkIn, checkOut);
        } else if (type != null) {
            rooms = roomUseCase.getAvailableRoomsByType(type);
        } else {
            rooms = roomUseCase.getAvailableRooms();
        }

        // CORRECTION #1 : masquer le numéro de chambre pour les clients
        if (isClient(authentication)) {
            List<ClientRoomResponse> clientRooms = rooms.stream()
                    .map(r -> new ClientRoomResponse(
                            r.id(), r.type(), r.price(), r.price_per_night(),
                            r.capacity(), r.status(), r.description(), r.amenities()))
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(clientRooms));
        }

        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    // ──────────────────────────── GET BY ID ────────────────────────────

    /**
     * GET /api/rooms/{id}
     *
     * CORRECTION #1 : CLIENT reçoit ClientRoomResponse (sans number).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<?>> getRoomById(
            @PathVariable Long id,
            Authentication authentication) {

        RoomResponse room = roomUseCase.getRoomById(id);

        if (isClient(authentication)) {
            ClientRoomResponse clientRoom = new ClientRoomResponse(
                    room.id(), room.type(), room.price(), room.price_per_night(),
                    room.capacity(), room.status(), room.description(), room.amenities());
            return ResponseEntity.ok(ApiResponse.success(clientRoom));
        }

        return ResponseEntity.ok(ApiResponse.success(room));
    }

    // ──────────────────────────── CREATE ────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {

        RoomResponse created = roomUseCase.createRoom(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chambre créée avec succès", created));
    }

    // ──────────────────────────── UPDATE ────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {

        RoomResponse updated = roomUseCase.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Chambre mise à jour avec succès", updated));
    }

    // ──────────────────────────── DELETE ────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomUseCase.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Chambre supprimée avec succès"));
    }
}
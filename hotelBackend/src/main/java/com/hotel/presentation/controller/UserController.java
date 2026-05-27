package com.hotel.presentation.controller;

import com.hotel.application.dto.request.ChangePasswordRequest;
import com.hotel.application.dto.request.UpdateUserRequest;
import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.UserResponse;
import com.hotel.application.usecase.UserUseCase;
import com.hotel.domain.model.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PRESENTATION LAYER — User management REST controller.
 * All routes require authentication. ADMIN-only operations are annotated.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    /**
     * GET /api/users
     * List all users. ADMIN & RECEPTIONNISTE only.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getAllUsers()));
    }

    /**
     * GET /api/users/role/{role}
     * List users by role.
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getUsersByRole(role)));
    }

    /**
     * GET /api/users/{id}
     * Get user by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getUserById(id)));
    }

    /**
     * PUT /api/users/{id}
     * Update user profile. ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse updated = userUseCase.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour", updated));
    }

    /**
     * PATCH /api/users/{id}/password
     * Change user password.
     */
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        userUseCase.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès"));
    }

    /**
     * PATCH /api/users/{id}/deactivate
     * Deactivate a user account. ADMIN only.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userUseCase.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Compte désactivé"));
    }

    /**
     * PATCH /api/users/{id}/activate
     * Reactivate a user account. ADMIN only.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        userUseCase.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Compte activé"));
    }

    /**
     * DELETE /api/users/{id}
     * Delete a user permanently. ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé"));
    }
}
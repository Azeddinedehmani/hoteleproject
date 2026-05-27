package com.hotel.application.usecase;

import com.hotel.application.dto.request.LoginRequest;
import com.hotel.application.dto.request.RegisterRequest;
import com.hotel.application.dto.response.AuthResponse;
import com.hotel.application.dto.response.UserResponse;

/**
 * APPLICATION LAYER — Auth Use Case Port
 * Defines what authentication operations are available.
 */
public interface AuthUseCase {

    /**
     * Register a new user and return JWT token.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate existing user and return JWT token.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Get current authenticated user profile.
     */
    UserResponse getCurrentUser(String email);
}
package com.hotel.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * APPLICATION LAYER — Outbound DTO returned after authentication.
 *
 * FIX: @JsonProperty("token") expose le champ sous le nom "token" dans le JSON,
 * ce qui correspond à json.data.token dans le script Postman auto-save.
 */
public record AuthResponse(
        @JsonProperty("token")
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String token, Long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
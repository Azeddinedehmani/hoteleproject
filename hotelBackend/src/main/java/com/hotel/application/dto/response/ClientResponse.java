package com.hotel.application.dto.response;

import com.hotel.domain.model.Client;

/**
 * APPLICATION LAYER — Read-only view of a Client.
 * Constructed directly from the domain model — no mapper needed for the response.
 */
public record ClientResponse(
        Long   id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String cin
) {
    /** Convenient factory — converts domain model → DTO. */
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getCin()
        );
    }
}
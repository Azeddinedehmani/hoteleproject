package com.hotel.domain.service;

import com.hotel.domain.model.User;

/**
 * DOMAIN LAYER — Domain Service Port
 * Contains domain-specific logic that doesn't belong to a single entity.
 */
public interface UserDomainService {

    /**
     * Encodes a raw password using the configured strategy.
     */
    String encodePassword(String rawPassword);

    /**
     * Verifies a raw password against an encoded one.
     */
    boolean passwordMatches(String rawPassword, String encodedPassword);

    /**
     * Validates that an email is not already taken.
     * Throws DomainException if email exists.
     */
    void ensureEmailIsUnique(String email);

    /**
     * Validates that a user exists and is active.
     */
    void ensureUserIsActive(User user);
}
package com.hotel.domain.repository;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — UserRepository Port
 * Interface defined in the domain; implemented in infrastructure.
 * The domain depends on this abstraction, NOT on JPA/Hibernate.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    void deleteById(Long id);

    long count();
}
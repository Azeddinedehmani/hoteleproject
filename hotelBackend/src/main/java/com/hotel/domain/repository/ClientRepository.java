package com.hotel.domain.repository;

import com.hotel.domain.model.Client;

import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — ClientRepository Port
 * Interface définie dans le domaine ; implémentée en infrastructure.
 * Le domaine dépend de cette abstraction, PAS de JPA/Hibernate.
 */
public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(Long id);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByCin(String cin);

    List<Client> findAll();

    boolean existsByEmail(String email);

    boolean existsByCin(String cin);

    void deleteById(Long id);

    long count();
}
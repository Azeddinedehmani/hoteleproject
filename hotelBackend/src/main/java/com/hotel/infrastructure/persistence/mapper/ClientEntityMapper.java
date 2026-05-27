package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Client;
import com.hotel.infrastructure.persistence.entity.ClientEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Bidirectional mapper between domain Client and JPA ClientEntity.
 * Keeps domain and persistence completely decoupled.
 */
@Component
public class ClientEntityMapper {

    /**
     * JPA entity → domain model (reconstitution depuis la BDD).
     */
    public Client toDomain(ClientEntity entity) {
        if (entity == null) return null;
        return Client.reconstitute(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCin()
        );
    }

    /**
     * Domain model → JPA entity (pour persistance).
     */
    public ClientEntity toEntity(Client client) {
        if (client == null) return null;
        return ClientEntity.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .cin(client.getCin())
                .build();
    }
}
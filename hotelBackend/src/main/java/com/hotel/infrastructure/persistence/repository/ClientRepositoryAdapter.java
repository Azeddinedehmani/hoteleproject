package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Client;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.infrastructure.persistence.mapper.ClientEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's ClientRepository port.
 * Pont entre le domaine et JPA.
 * Le domaine dépend de l'interface ; Spring injecte cette implémentation.
 */
@Component
@RequiredArgsConstructor
public class ClientRepositoryAdapter implements ClientRepository {

    private final JpaClientRepository jpaClientRepository;
    private final ClientEntityMapper  mapper;

    @Override
    public Client save(Client client) {
        var entity = mapper.toEntity(client);
        var saved  = jpaClientRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return jpaClientRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return jpaClientRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByCin(String cin) {
        return jpaClientRepository.findByCin(cin)
                .map(mapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return jpaClientRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaClientRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByCin(String cin) {
        return jpaClientRepository.existsByCin(cin);
    }

    @Override
    public void deleteById(Long id) {
        jpaClientRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaClientRepository.count();
    }
}
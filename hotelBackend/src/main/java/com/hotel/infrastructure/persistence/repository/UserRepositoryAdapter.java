package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import com.hotel.infrastructure.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's UserRepository port.
 * This is the bridge between domain and JPA.
 * The domain depends on the interface; Spring injects this implementation.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserEntityMapper mapper;

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        var saved  = jpaUserRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        return jpaUserRepository.findByRole(role)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaUserRepository.count();
    }
}
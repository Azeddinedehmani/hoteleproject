package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.model.Tariff.Season;
import com.hotel.domain.repository.TariffRepository;
import com.hotel.infrastructure.persistence.mapper.TariffEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's TariffRepository port.
 * Bridge between the domain and JPA.
 */
@Component
@RequiredArgsConstructor
public class TariffRepositoryAdapter implements TariffRepository {

    private final JpaTariffRepository jpaRepo;
    private final TariffEntityMapper  mapper;

    @Override
    public Tariff save(Tariff tariff) {
        var entity = mapper.toEntity(tariff);
        var saved  = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tariff> findById(Long id) {
        return jpaRepo.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Tariff> findAll() {
        return jpaRepo.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Tariff> findBySeason(Season season) {
        return jpaRepo.findBySeason(season)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Tariff> findByRoomType(RoomType roomType) {
        return jpaRepo.findByRoomType(roomType)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Tariff> findByActive(boolean active) {
        return jpaRepo.findByActive(active)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }

    /**
     * Délègue la recherche optimisée à la requête JPQL de JpaTariffRepository.
     * Le tri (spécifique avant générique, puis prix croissant) et le filtre
     * de couverture inclusive sont assurés côté base de données.
     */
    @Override
    public Optional<Tariff> findTariffForRoomAndDates(RoomType roomType,
                                                       LocalDate checkIn,
                                                       LocalDate checkOut) {
        return jpaRepo.findApplicableTariffs(roomType, checkIn, checkOut)
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }
}
package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.repository.RoomRepository;
import com.hotel.infrastructure.persistence.mapper.RoomEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adapter implementing domain's RoomRepository port.
 * Pont entre le domaine et JPA.
 * Le domaine dépend de l'interface ; Spring injecte cette implémentation.
 */
@Component
@RequiredArgsConstructor
public class RoomRepositoryAdapter implements RoomRepository {

    private final JpaRoomRepository jpaRoomRepository;
    private final RoomEntityMapper  mapper;

    @Override
    public Room save(Room room) {
        var entity = mapper.toEntity(room);
        var saved  = jpaRoomRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return jpaRoomRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Room> findByNumber(String number) {
        return jpaRoomRepository.findByNumber(number)
                .map(mapper::toDomain);
    }

    @Override
    public List<Room> findAll() {
        return jpaRoomRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAllAvailable() {
        return jpaRoomRepository.findByStatus(RoomStatus.AVAILABLE)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAvailableByType(RoomType type) {
        return jpaRoomRepository.findByStatusAndType(RoomStatus.AVAILABLE, type)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAllByType(RoomType type) {
        return jpaRoomRepository.findByType(type)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut) {
        return jpaRoomRepository.findAvailableForDates(checkIn, checkOut)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAvailableForDatesByType(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        return jpaRoomRepository.findAvailableForDatesByType(checkIn, checkOut, type)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumber(String number) {
        return jpaRoomRepository.existsByNumber(number);
    }

    @Override
    public void deleteById(Long id) {
        jpaRoomRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRoomRepository.count();
    }
}
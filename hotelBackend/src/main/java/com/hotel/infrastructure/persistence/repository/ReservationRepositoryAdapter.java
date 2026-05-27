package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.infrastructure.persistence.mapper.ReservationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — Adaptateur implémentant le port domaine ReservationRepository.
 *
 * Pont entre le domaine et JPA ; jamais référencé directement par les couches
 * application ou domaine (seule l'interface {@link ReservationRepository} est injectée).
 *
 * POINT 11 — Implémentation de {@link #allRoomsOccupiedForType} :
 * délègue à {@link JpaReservationRepository#countAvailableRoomsOfType} ; retourne
 * {@code true} (aucune chambre libre) si le compte est 0.
 */
@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationEntityMapper  mapper;

    // ── Persitance ─────────────────────────────────────────────────────────────

    @Override
    public Reservation save(Reservation reservation) {
        var entity = mapper.toEntity(reservation);
        var saved  = jpaReservationRepository.save(entity);
        return mapper.toDomain(saved);
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return jpaReservationRepository.findByClientId(clientId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByRoomId(Long roomId) {
        return jpaReservationRepository.findByRoomId(roomId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return jpaReservationRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    // ── Disponibilité par chambre précise ──────────────────────────────────────

    @Override
    public boolean hasOverlappingReservation(Long roomId, LocalDate checkIn,
                                              LocalDate checkOut, Long excludeId) {
        return jpaReservationRepository.existsOverlap(roomId, checkIn, checkOut, excludeId);
    }

    // ── POINT 11 — Disponibilité par type de chambre ───────────────────────────

    /**
     * POINT 11 — Retourne {@code true} si aucune chambre du type {@code roomType}
     * n'est disponible sur la plage [checkIn, checkOut[.
     *
     * Délègue à la requête JPQL {@code countAvailableRoomsOfType} :
     * si le nombre de chambres libres est 0 → toutes occupées → {@code true}.
     */
    @Override
    public boolean allRoomsOccupiedForType(RoomType roomType, LocalDate checkIn,
                                           LocalDate checkOut, Long excludeId) {
        long disponibles = jpaReservationRepository
                .countAvailableRoomsOfType(roomType, checkIn, checkOut, excludeId);
        return disponibles == 0;
    }

    // ── Suppression ────────────────────────────────────────────────────────────

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaReservationRepository.count();
    }
}
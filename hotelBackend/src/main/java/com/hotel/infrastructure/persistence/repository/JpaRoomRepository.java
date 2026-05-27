package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.infrastructure.persistence.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for RoomEntity.
 * Only used by RoomRepositoryAdapter. Never injected directly into domain or application layers.
 */
public interface JpaRoomRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByNumber(String number);

    boolean existsByNumber(String number);

    List<RoomEntity> findByStatus(RoomStatus status);

    List<RoomEntity> findByType(RoomType type);

    List<RoomEntity> findByStatusAndType(RoomStatus status, RoomType type);

    /**
     * Returns all AVAILABLE rooms, ignoring date parameters for now.
     *
     * TODO: When ReservationEntity is implemented, replace this default method with:
     *
     * @Query("""
     *     SELECT r FROM RoomEntity r
     *     WHERE r.status = 'AVAILABLE'
     *       AND r.id NOT IN (
     *           SELECT res.room.id FROM ReservationEntity res
     *           WHERE res.checkIn < :checkOut AND res.checkOut > :checkIn
     *       )""")
     * List<RoomEntity> findAvailableForDates(@Param("checkIn") LocalDate checkIn,
     *                                        @Param("checkOut") LocalDate checkOut);
     */
    default List<RoomEntity> findAvailableForDates(LocalDate checkIn, LocalDate checkOut) {
        return findByStatus(RoomStatus.AVAILABLE);
    }

    /**
     * Returns all AVAILABLE rooms of the given type, ignoring date parameters for now.
     *
     * TODO: When ReservationEntity is implemented, replace this default method with a
     *       @Query that adds the NOT IN reservation-overlap subquery (same pattern as above).
     */
    default List<RoomEntity> findAvailableForDatesByType(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        return findByStatusAndType(RoomStatus.AVAILABLE, type);
    }
}
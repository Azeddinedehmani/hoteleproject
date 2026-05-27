package com.hotel.domain.repository;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomStatus;
import com.hotel.domain.model.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — RoomRepository Port
 * Interface définie dans le domaine ; implémentée en infrastructure.
 * Le domaine dépend de cette abstraction, PAS de JPA/Hibernate.
 */
public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findById(Long id);

    Optional<Room> findByNumber(String number);

    List<Room> findAll();

    /** Rooms whose status is AVAILABLE */
    List<Room> findAllAvailable();

    /** Filter available rooms by type */
    List<Room> findAvailableByType(RoomType type);

    /** Filter all rooms by type */
    List<Room> findAllByType(RoomType type);

    /**
     * Rooms that are available for the requested date range.
     * A room is available if it has no overlapping confirmed reservation.
     */
    List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut);

    /**
     * Rooms available for the given dates, optionally filtered by type.
     */
    List<Room> findAvailableForDatesByType(LocalDate checkIn, LocalDate checkOut, RoomType type);

    boolean existsByNumber(String number);

    void deleteById(Long id);

    long count();
}
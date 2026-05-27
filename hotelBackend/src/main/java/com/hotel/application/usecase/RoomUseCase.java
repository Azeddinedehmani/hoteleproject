package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateRoomRequest;
import com.hotel.application.dto.request.UpdateRoomRequest;
import com.hotel.application.dto.response.RoomResponse;
import com.hotel.domain.model.RoomType;

import java.time.LocalDate;
import java.util.List;

/**
 * APPLICATION LAYER — RoomUseCase Port (inbound).
 * Defines all operations available on the Room aggregate.
 */
public interface RoomUseCase {

    /** GET /api/rooms */
    List<RoomResponse> getAllRooms();

    /** GET /api/rooms?type=DOUBLE */
    List<RoomResponse> getRoomsByType(RoomType type);

    /** GET /api/rooms/{id} */
    RoomResponse getRoomById(Long id);

    /** GET /api/rooms/available */
    List<RoomResponse> getAvailableRooms();

    /** GET /api/rooms/available?type=SUITE */
    List<RoomResponse> getAvailableRoomsByType(RoomType type);

    /**
     * GET /api/rooms/available?checkIn=2025-06-01&checkOut=2025-06-05
     * Returns rooms not booked for the requested date range.
     */
    List<RoomResponse> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut);

    /**
     * GET /api/rooms/available?checkIn=...&checkOut=...&type=SUITE
     */
    List<RoomResponse> getAvailableRoomsForDatesByType(LocalDate checkIn, LocalDate checkOut, RoomType type);

    /** POST /api/rooms */
    RoomResponse createRoom(CreateRoomRequest request);

    /** PUT /api/rooms/{id} */
    RoomResponse updateRoom(Long id, UpdateRoomRequest request);

    /** DELETE /api/rooms/{id} */
    void deleteRoom(Long id);
}
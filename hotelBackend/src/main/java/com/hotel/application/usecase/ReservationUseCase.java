package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateReservationRequest;
import com.hotel.application.dto.request.UpdateReservationRequest;
import com.hotel.application.dto.response.ReservationResponse;

import java.util.List;

/**
 * APPLICATION LAYER — ReservationUseCase Port (inbound).
 * Defines all operations available on the Reservation aggregate.
 */
public interface ReservationUseCase {

    /** GET /api/reservations */
    List<ReservationResponse> getAllReservations();

    /** GET /api/reservations/my — reservations for the authenticated client */
    List<ReservationResponse> getMyReservations(Long clientId);

    /** GET /api/reservations/{id} */
    ReservationResponse getReservationById(Long id);

    /** POST /api/reservations */
    ReservationResponse createReservation(CreateReservationRequest request);

    /** PUT /api/reservations/{id} */
    ReservationResponse updateReservation(Long id, UpdateReservationRequest request);

    /** PATCH /api/reservations/{id}/cancel */
    ReservationResponse cancelReservation(Long id);

    /** PATCH /api/reservations/{id}/assign-room — attribue une chambre avant le check-in */
    ReservationResponse assignRoom(Long id, Long roomId);

    /** PATCH /api/reservations/{id}/check-in */
    ReservationResponse checkIn(Long id);

    /** PATCH /api/reservations/{id}/check-out */
    ReservationResponse checkOut(Long id);

    /** DELETE /api/reservations/{id} */
    void deleteReservation(Long id);
}
package com.hotel.infrastructure.persistence.mapper;

import com.hotel.domain.model.Reservation;
import com.hotel.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — Mapper Reservation domain ↔ JPA entity
 *
 * POINT 10 — Prise en charge de {@code roomType} nullable :
 *   - {@code roomId} peut être null (réservation par type non encore attribuée)
 *   - {@code roomType} est persisté dans {@link ReservationEntity}
 */
@Component
public class ReservationEntityMapper {

    public ReservationEntity toEntity(Reservation domain) {
        return ReservationEntity.builder()
                .id(domain.getId())
                .clientId(domain.getClientId())
                .roomId(domain.getRoomId())               // nullable
                .roomType(domain.getRoomType())           // nullable
                .checkInDate(domain.getCheckInDate())
                .checkOutDate(domain.getCheckOutDate())
                .status(domain.getStatus())
                .guests(domain.getGuests())
                .notes(domain.getNotes())
                .actualCheckInAt(domain.getActualCheckInAt())
                .actualCheckOutAt(domain.getActualCheckOutAt())
                .build();
    }

    public Reservation toDomain(ReservationEntity entity) {
        return Reservation.reconstitute(
                entity.getId(),
                entity.getClientId(),
                entity.getRoomId(),                       // nullable
                entity.getRoomType(),                     // nullable
                entity.getCheckInDate(),
                entity.getCheckOutDate(),
                entity.getStatus(),
                entity.getGuests(),
                entity.getNotes(),
                entity.getActualCheckInAt(),
                entity.getActualCheckOutAt()
        );
    }
}
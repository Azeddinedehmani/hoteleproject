package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.RoomType;
import com.hotel.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository pour ReservationEntity.
 *
 * POINT 11 — Ajout de {@link #existsOverlapByType} :
 * vérifie qu'au moins une chambre du type demandé est libre sur la plage de dates.
 * La requête JPQL joint ReservationEntity et RoomEntity via roomId pour filtrer
 * sur le type de chambre.
 */
public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByClientId(Long clientId);

    List<ReservationEntity> findByRoomId(Long roomId);

    List<ReservationEntity> findByStatus(ReservationStatus status);

    // ── Chevauchement par chambre précise ──────────────────────────────────────

    /**
     * Retourne {@code true} si la chambre {@code roomId} a au moins une réservation
     * active (non annulée, non terminée) chevauchant la plage demandée.
     *
     * Condition de chevauchement : existing.checkIn < requested.checkOut
     *                          AND existing.checkOut > requested.checkIn
     *
     * @param excludeId  identifiant à exclure lors d'une mise à jour ; null = tout inclure
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM ReservationEntity r
            WHERE r.roomId = :roomId
              AND r.status NOT IN ('CANCELLED', 'CHECKED_OUT')
              AND r.checkInDate  < :checkOut
              AND r.checkOutDate > :checkIn
              AND (:excludeId IS NULL OR r.id <> :excludeId)
            """)
    boolean existsOverlap(@Param("roomId")    Long roomId,
                          @Param("checkIn")   LocalDate checkIn,
                          @Param("checkOut")  LocalDate checkOut,
                          @Param("excludeId") Long excludeId);

    // ── POINT 11 — Chevauchement par type de chambre ───────────────────────────

    /**
     * POINT 11 — Compte le nombre de chambres du type {@code roomType} actuellement
     * disponibles (sans réservation active qui chevauche la plage demandée).
     *
     * <p>La requête :
     * <ol>
     *   <li>Identifie toutes les chambres du type via {@code RoomEntity}</li>
     *   <li>Soustrait celles qui ont une réservation active (PENDING, CONFIRMED,
     *       CHECKED_IN) chevauchant [checkIn, checkOut[</li>
     *   <li>Retourne le nombre de chambres encore libres</li>
     * </ol>
     *
     * <p>Une chambre « libre » = elle n'apparaît dans aucune réservation active
     * qui chevauche la plage demandée.
     *
     * @param roomType   type de chambre à vérifier
     * @param checkIn    début de la plage demandée (inclus)
     * @param checkOut   fin de la plage demandée (exclus)
     * @param excludeId  réservation à ignorer (mise à jour) ; null = tout inclure
     * @return nombre de chambres du type disponibles sur cette plage
     */
    @Query("""
            SELECT COUNT(ro)
            FROM RoomEntity ro
            WHERE ro.type = :roomType
              AND ro.id NOT IN (
                  SELECT r.roomId
                  FROM ReservationEntity r
                  WHERE r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
                    AND r.checkInDate  < :checkOut
                    AND r.checkOutDate > :checkIn
                    AND (:excludeId IS NULL OR r.id <> :excludeId)
              )
            """)
    long countAvailableRoomsOfType(@Param("roomType")  RoomType roomType,
                                   @Param("checkIn")   LocalDate checkIn,
                                   @Param("checkOut")  LocalDate checkOut,
                                   @Param("excludeId") Long excludeId);
}
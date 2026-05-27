package com.hotel.infrastructure.persistence.repository;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff.Season;
import com.hotel.infrastructure.persistence.entity.TariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * INFRASTRUCTURE LAYER — Spring Data JPA repository for TariffEntity.
 * Only used by TariffRepositoryAdapter.
 */
public interface JpaTariffRepository extends JpaRepository<TariffEntity, Long> {

    List<TariffEntity> findBySeason(Season season);

    List<TariffEntity> findByRoomType(RoomType roomType);

    List<TariffEntity> findByActive(boolean active);

    /**
     * Retourne le meilleur tarif actif couvrant la période checkIn..checkOut
     * pour le type de chambre donné.
     *
     * Critères :
     *   - active = true
     *   - startDate <= :checkIn  ET  endDate >= :checkOut  (couverture INCLUSIVE)
     *   - roomType = :roomType  OU  roomType IS NULL
     *
     * Tri : spécifique (roomType non-null = 0) avant générique (= 1),
     *       puis prix effectif = pricePerNight * (1 - discountPercent/100) croissant.
     * LIMIT 1 via la pagination Spring Data (premier résultat).
     */
    @Query("""
            SELECT t FROM TariffEntity t
            WHERE t.active = true
              AND t.startDate <= :checkIn
              AND t.endDate   >= :checkOut
              AND (t.roomType = :roomType OR t.roomType IS NULL)
            ORDER BY
              CASE WHEN t.roomType IS NULL THEN 1 ELSE 0 END ASC,
              t.pricePerNight * (1 - t.discountPercent / 100) ASC
            """)
    List<TariffEntity> findApplicableTariffs(
            @Param("roomType")  RoomType  roomType,
            @Param("checkIn")   LocalDate checkIn,
            @Param("checkOut")  LocalDate checkOut);
}
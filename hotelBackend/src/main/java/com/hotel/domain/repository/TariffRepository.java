package com.hotel.domain.repository;

import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.model.Tariff.Season;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — Tariff Repository Port.
 * Clean interface, no framework dependency.
 */
public interface TariffRepository {

    Tariff save(Tariff tariff);

    Optional<Tariff> findById(Long id);

    List<Tariff> findAll();

    List<Tariff> findBySeason(Season season);

    List<Tariff> findByRoomType(RoomType roomType);

    List<Tariff> findByActive(boolean active);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();

    /**
     * Recherche le meilleur tarif actif couvrant intégralement la période checkIn..checkOut
     * pour le type de chambre donné.
     *
     * Règles de sélection :
     *   1. Tarif actif (active = true)
     *   2. startDate <= checkIn ET endDate >= checkOut  (couverture inclusive)
     *   3. roomType = :roomType  OU  roomType IS NULL   (spécifique ou générique)
     *   4. Tri : spécifique (roomType non-null) avant générique, puis prix effectif croissant
     *   5. LIMIT 1 — on retourne le meilleur candidat
     *
     * @param roomType  type de chambre de la réservation
     * @param checkIn   date d'arrivée
     * @param checkOut  date de départ
     * @return Optional contenant le tarif le plus adapté, ou empty si aucun tarif applicable
     */
    Optional<Tariff> findTariffForRoomAndDates(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
}
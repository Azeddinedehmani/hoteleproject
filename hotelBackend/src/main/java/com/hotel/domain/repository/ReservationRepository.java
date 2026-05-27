package com.hotel.domain.repository;

import com.hotel.domain.model.Reservation;
import com.hotel.domain.model.ReservationStatus;
import com.hotel.domain.model.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DOMAIN LAYER — Port ReservationRepository
 *
 * Interface définie dans le domaine ; implémentée en infrastructure.
 * Le domaine dépend de cette abstraction, PAS de JPA/Hibernate.
 *
 * POINT 11 — Ajout de {@link #hasOverlappingByType} pour la validation
 * des chevauchements par type de chambre (logique demandée en POINT 10 :
 * la chambre précise n'est pas encore attribuée lors de la réservation client).
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    /** Toutes les réservations d'un client donné. */
    List<Reservation> findByClientId(Long clientId);

    /** Toutes les réservations d'une chambre donnée. */
    List<Reservation> findByRoomId(Long roomId);

    /** Toutes les réservations filtrées par statut. */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Vérifie si une chambre précise a une réservation active qui chevauche
     * la plage de dates demandée. Utilisé pour éviter le double-booking
     * lorsqu'une chambre spécifique est ciblée (rôles ADMIN / RECEPTIONNISTE).
     *
     * @param roomId     identifiant de la chambre à vérifier
     * @param checkIn    début de la plage demandée
     * @param checkOut   fin de la plage demandée
     * @param excludeId  identifiant de réservation à exclure (mise à jour) ; null = tout inclure
     */
    boolean hasOverlappingReservation(Long roomId, LocalDate checkIn,
                                      LocalDate checkOut, Long excludeId);

    /**
     * POINT 11 — Vérifie si toutes les chambres d'un type donné sont occupées
     * (aucune disponible) sur la plage de dates demandée.
     *
     * Une chambre est considérée indisponible si au moins une réservation active
     * (statuts PENDING, CONFIRMED, CHECKED_IN) chevauche les dates demandées.
     *
     * Retourne {@code true} si aucune chambre du type {@code roomType} n'est libre,
     * {@code false} s'il en reste au moins une disponible.
     *
     * @param roomType   type de chambre à vérifier (SIMPLE, DOUBLE, SUITE, DELUXE…)
     * @param checkIn    début de la plage demandée
     * @param checkOut   fin de la plage demandée
     * @param excludeId  réservation à exclure (mise à jour) ; null = tout inclure
     */
    boolean allRoomsOccupiedForType(RoomType roomType, LocalDate checkIn,
                                    LocalDate checkOut, Long excludeId);

    void deleteById(Long id);

    long count();
}
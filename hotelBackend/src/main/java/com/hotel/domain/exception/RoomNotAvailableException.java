package com.hotel.domain.exception;

import com.hotel.domain.model.RoomType;

/**
 * DOMAIN LAYER — Levée quand aucune chambre n'est disponible pour les dates demandées.
 *
 * POINT 11 — Deux constructeurs :
 * <ul>
 *   <li>par chambre précise (ADMIN / RECEPTIONNISTE) : inchangé</li>
 *   <li>par type de chambre (CLIENT) : nouveau, message orienté client sans numéro interne</li>
 * </ul>
 */
public class RoomNotAvailableException extends DomainException {

    /**
     * Chambre précise non disponible — usage interne ADMIN / RECEPTIONNISTE.
     *
     * @param roomId   identifiant de la chambre
     * @param checkIn  date d'arrivée demandée
     * @param checkOut date de départ demandée
     */
    public RoomNotAvailableException(Long roomId, String checkIn, String checkOut) {
        super("La chambre #" + roomId + " n'est pas disponible du " + checkIn + " au " + checkOut);
    }

    /**
     * POINT 11 — Aucune chambre du type demandé n'est libre — message destiné au client.
     * N'expose aucun numéro de chambre interne.
     *
     * @param roomType type de chambre demandé
     * @param checkIn  date d'arrivée demandée
     * @param checkOut date de départ demandée
     */
    public RoomNotAvailableException(RoomType roomType, String checkIn, String checkOut) {
        super("Aucune chambre de type " + roomType.name()
                + " n'est disponible du " + checkIn + " au " + checkOut
                + ". Veuillez choisir d'autres dates ou un type différent.");
    }
}
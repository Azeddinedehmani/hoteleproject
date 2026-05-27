package com.hotel.application.usecase;

import com.hotel.application.dto.request.ApplyDiscountRequest;
import com.hotel.application.dto.request.CreateTariffRequest;
import com.hotel.application.dto.request.UpdateTariffRequest;
import com.hotel.application.dto.response.TariffResponse;
import com.hotel.domain.model.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * APPLICATION LAYER — Tariff Use Case Port.
 *
 * CORRECTION #4 : ajout de getApplicableTariff(roomType, checkIn, checkOut).
 * Retourne le tarif actif qui couvre la période demandée pour le type de chambre donné.
 */
public interface TariffUseCase {

    List<TariffResponse> getAllTariffs();

    TariffResponse getTariffById(Long id);

    TariffResponse createTariff(CreateTariffRequest request);

    TariffResponse updateTariff(Long id, UpdateTariffRequest request);

    TariffResponse applyDiscount(Long id, ApplyDiscountRequest request);

    void deleteTariff(Long id);

    /**
     * CORRECTION #4 — Retourne le tarif applicable pour un type de chambre
     * et une période de séjour donnée.
     *
     * Règles de sélection (par ordre de priorité) :
     *   1. Le tarif doit être actif (active = true)
     *   2. Sa fenêtre de dates (startDate..endDate) doit couvrir la période checkIn..checkOut
     *   3. Son roomType doit correspondre (ou être null = applicable à tous les types)
     *   4. En cas de plusieurs candidats, on préfère le tarif le plus spécifique
     *      (roomType non null > roomType null), puis le prix effectif le plus bas.
     *
     * @param roomType  type de la chambre (SIMPLE, DOUBLE, SUITE, DELUXE…)
     * @param checkIn   date d'arrivée souhaitée
     * @param checkOut  date de départ souhaitée
     * @return le meilleur tarif applicable, ou Optional.empty() s'il n'en existe aucun
     */
    Optional<TariffResponse> getApplicableTariff(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
}
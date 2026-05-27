package com.hotel.application.usecase;

import com.hotel.domain.model.Room;
import com.hotel.domain.model.RoomType;
import com.hotel.domain.model.Tariff;
import com.hotel.domain.repository.RoomRepository;
import com.hotel.domain.repository.TariffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * SCHEDULER — Mise à jour automatique des prix des chambres.
 *
 * Toutes les heures, ce composant :
 *   1. Cherche le tarif actif pour chaque type de chambre (aujourd'hui).
 *   2. Si un tarif actif existe → met à jour le prix de la chambre.
 *   3. Si AUCUN tarif actif → NE TOUCHE PAS au prix existant (on conserve
 *      le prix de base défini manuellement dans AdminTariffs).
 *
 * Règle clé : on ne met jamais price = null via le scheduler.
 */
@Component
public class RoomPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(RoomPriceScheduler.class);

    private final RoomRepository   roomRepository;
    private final TariffRepository tariffRepository;

    public RoomPriceScheduler(RoomRepository roomRepository, TariffRepository tariffRepository) {
        this.roomRepository  = roomRepository;
        this.tariffRepository = tariffRepository;
    }

    @Scheduled(fixedRate = 3_600_000, initialDelay = 10_000)
    @Transactional
    public void syncRoomPricesWithTariffs() {
        log.info("[Scheduler] Synchronisation des prix des chambres avec les tarifs actifs...");

        LocalDate today = LocalDate.now();
        List<Tariff> activeTariffs = tariffRepository.findByActive(true)
                .stream()
                .filter(t -> !today.isBefore(t.getStartDate()) && !today.isAfter(t.getEndDate()))
                .toList();

        if (activeTariffs.isEmpty()) {
            log.info("[Scheduler] Aucun tarif actif aujourd'hui — prix des chambres inchangés.");
            return;
        }

        List<Room> allRooms = roomRepository.findAll();
        int updated = 0;

        for (Room room : allRooms) {
            Optional<BigDecimal> tariffPrice = resolveTariffPrice(room.getType(), activeTariffs);

            // Si aucun tarif ne correspond à ce type → on ne touche pas au prix
            if (tariffPrice.isEmpty()) {
                log.debug("  Chambre #{} type={} : pas de tarif correspondant — prix conservé ({})",
                        room.getNumber(), room.getType(), room.getPrice());
                continue;
            }

            BigDecimal newPrice = tariffPrice.get();
            BigDecimal current  = room.getPrice();

            // Ne mettre à jour que si le prix a vraiment changé
            if (current == null || newPrice.compareTo(current) != 0) {
                room.updateDetails(
                        room.getNumber(), room.getType(), newPrice,
                        room.getCapacity(), room.getDescription(),
                        room.getAmenities()
                );
                roomRepository.save(room);
                updated++;
                log.debug("  Chambre #{} type={} : {} → {}",
                        room.getNumber(), room.getType(), current, newPrice);
            }
        }

        log.info("[Scheduler] {} chambre(s) mise(s) à jour.", updated);
    }

    /**
     * Retourne le prix effectif du meilleur tarif actif pour ce type,
     * ou Optional.empty() si aucun tarif ne correspond.
     */
    private Optional<BigDecimal> resolveTariffPrice(RoomType type, List<Tariff> activeTariffs) {
        return activeTariffs.stream()
                .filter(t -> t.getRoomType() == null || t.getRoomType() == type)
                .min(Comparator
                        .<Tariff, Integer>comparing(t -> t.getRoomType() == null ? 1 : 0)
                        .thenComparing(Tariff::effectivePrice))
                .map(Tariff::effectivePrice);
    }
}
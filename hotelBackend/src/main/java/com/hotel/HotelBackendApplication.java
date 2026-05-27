package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hotel Management Backend — Clean Architecture
 * Layers: domain → application → infrastructure → presentation
 *
 * FIX : @EnableScheduling ajouté pour activer le RoomPriceScheduler
 *       (synchronisation automatique des prix des chambres avec les tarifs actifs).
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class HotelBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBackendApplication.class, args);
    }
}
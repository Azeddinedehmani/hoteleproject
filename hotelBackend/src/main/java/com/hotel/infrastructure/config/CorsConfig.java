package com.hotel.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * INFRASTRUCTURE LAYER — CORS configuration.
 *
 * FIX PROBLÈME 2 :
 *   Le bean {@code CorsFilter} a été SUPPRIMÉ.
 *   Spring Security applique déjà la configuration CORS via
 *   {@code http.cors(c -> c.configurationSource(corsConfigurationSource))}.
 *   Avoir simultanément un {@code CorsFilter} ET la config Spring Security provoque
 *   des doublons de headers CORS (Access-Control-Allow-Origin apparaît deux fois),
 *   ce qui bloque tous les appels depuis le navigateur.
 *
 *   SOLUTION : exposer uniquement le bean {@code CorsConfigurationSource} ;
 *   SecurityConfig l'injecte via {@code @RequiredArgsConstructor} et l'utilise
 *   dans {@code http.cors(...)}.
 *
 *   RAPPEL : injection en {@code String} puis split manuel sur la virgule pour éviter
 *   le bug d'injection de {@code List<String>} avec des valeurs CSV
 *   (Spring injecterait la chaîne entière comme un seul élément de liste).
 */
@Configuration
public class CorsConfig {

    /** Origines autorisées — séparées par des virgules dans application.properties. */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsRaw;

    /**
     * Source de configuration CORS consommée par Spring Security.
     *
     * NOTE : Ce bean est détecté automatiquement par SecurityConfig via injection
     * {@code CorsConfigurationSource corsConfigurationSource} (cf. SecurityConfig).
     * Ne PAS déclarer de {@code CorsFilter} en plus — Spring Security l'applique déjà.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // FIX : split manuel sur la virgule + trim de chaque valeur
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        config.setAllowedOrigins(origins);

        // Tous les verbes HTTP + OPTIONS (preflight)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers nécessaires pour JWT
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        // Cache le résultat du preflight pendant 1 heure
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
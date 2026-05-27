package com.hotel.infrastructure.config;

import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * INFRASTRUCTURE LAYER — Seeds default users on startup.
 *
 * CORRECTION : @Profile("dev") SUPPRIMÉ.
 * Avec ce profil, le seeding ne s'exécutait jamais en mode par défaut,
 * laissant la base vide → impossible de se connecter.
 *
 * Le seeding est idempotent : existsByEmail() empêche les doublons.
 * En production, changer les mots de passe après le premier démarrage.
 *
 * Comptes créés :
 *   ADMIN        → admin@hotel.com     / Admin@1234
 *   RECEPTIONNISTE → reception@hotel.com / Recep@1234
 *   CLIENT (test)  → client@hotel.com    / Client@1234
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seedDefaultUsers() {
        return args -> {
            seedAdmin();
            seedReceptionniste();
            seedClientTest();
            log.info("✅ Data initialization complete — {} users in DB", userRepository.count());
        };
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@hotel.com")) {
            log.info("Admin already exists — skipping");
            return;
        }
        User admin = User.create(
                "Administrateur",
                "admin@hotel.com",
                passwordEncoder.encode("Admin@1234"),
                Role.ADMIN
        );
        userRepository.save(admin);
        log.info("✅ Admin account created → admin@hotel.com / Admin@1234");
    }

    private void seedReceptionniste() {
        if (userRepository.existsByEmail("reception@hotel.com")) {
            log.info("Receptionniste already exists — skipping");
            return;
        }
        User reception = User.create(
                "Réceptionniste Démo",
                "reception@hotel.com",
                passwordEncoder.encode("Recep@1234"),
                Role.RECEPTIONNISTE
        );
        userRepository.save(reception);
        log.info("✅ Receptionniste account created → reception@hotel.com / Recep@1234");
    }

    private void seedClientTest() {
        if (userRepository.existsByEmail("client@hotel.com")) {
            log.info("Client test already exists — skipping");
            return;
        }
        User client = User.create(
                "Client Test",
                "client@hotel.com",
                passwordEncoder.encode("Client@1234"),
                Role.CLIENT
        );
        userRepository.save(client);
        log.info("✅ Client test account created → client@hotel.com / Client@1234");
    }
}
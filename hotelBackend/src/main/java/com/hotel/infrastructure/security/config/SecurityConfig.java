package com.hotel.infrastructure.security.config;

import com.hotel.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * INFRASTRUCTURE LAYER — Spring Security configuration.
 * Stateless JWT-based authentication with role-based access control.
 *
 * CORRECTION #3 :
 *   - GET /reservations (liste complète) → ADMIN, RECEPTIONNISTE uniquement
 *   - GET /reservations/my              → CLIENT uniquement (géré dans le controller)
 *   - GET /reservations/{id}            → ADMIN, RECEPTIONNISTE, CLIENT
 *     (vérification de propriété faite dans ReservationController)
 *
 * CORRECTION #4 :
 *   - GET /tariffs/applicable           → CLIENT autorisé pour le calcul de prix saisonnier
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    // ────────────────── Public endpoints ──────────────────
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            .authorizeHttpRequests(auth -> auth

                    // OPTIONS preflight — toujours permis, sans token
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Endpoints publics (login, register, swagger)
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                    // ── Users ──
                    .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers("/users/**").hasRole("ADMIN")

                    // ── Clients ──
                    .requestMatchers(HttpMethod.GET,    "/clients/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers(HttpMethod.POST,   "/clients/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers(HttpMethod.PUT,    "/clients/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers(HttpMethod.DELETE, "/clients/**").hasRole("ADMIN")

                    // ── Rooms ──
                    .requestMatchers(HttpMethod.GET,    "/rooms/**").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers(HttpMethod.POST,   "/rooms/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/rooms/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")

                    // ── Reservations ──
                    // CORRECTION #3 : GET liste complète → ADMIN + RECEPTIONNISTE seulement
                    // CLIENT ne peut que créer (POST), voir les siennes (/my, /{id}) et annuler (PATCH cancel)
                    .requestMatchers(HttpMethod.GET,    "/reservations/my").hasRole("CLIENT")
                    .requestMatchers(HttpMethod.GET,    "/reservations").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers(HttpMethod.GET,    "/reservations/{id}").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers(HttpMethod.POST,   "/reservations/**").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers(HttpMethod.PUT,    "/reservations/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers(HttpMethod.PATCH,  "/reservations/**").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers(HttpMethod.DELETE, "/reservations/**").hasRole("ADMIN")

                    // ── Tariffs ──
                    // CORRECTION #4 : GET /tariffs/applicable → CLIENT autorisé (calcul prix saisonnier)
                    .requestMatchers(HttpMethod.GET, "/tariffs/applicable").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers(HttpMethod.GET, "/tariffs/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers("/tariffs/**").hasRole("ADMIN")

                    // ── Equipment ──
                    .requestMatchers(HttpMethod.GET, "/equipment/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")
                    .requestMatchers("/equipment/**").hasRole("ADMIN")

                    // ── Invoices ──
                    .requestMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("ADMIN", "RECEPTIONNISTE", "CLIENT")
                    .requestMatchers("/invoices/**").hasAnyRole("ADMIN", "RECEPTIONNISTE")

                    .anyRequest().authenticated()
            )

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider())

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
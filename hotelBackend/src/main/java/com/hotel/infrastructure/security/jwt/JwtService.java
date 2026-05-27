package com.hotel.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * INFRASTRUCTURE LAYER — JWT generation & validation service.
 *
 * FIX PROBLÈME 3 :
 *   1. Le secret est documenté clairement : il DOIT être remplacé en production.
 *   2. Un {@code @PostConstruct} valide que le secret fait au moins 32 caractères
 *      et qu'il ne correspond pas à la valeur par défaut connue en environnement
 *      non-développement (profil != "dev"). → IllegalStateException au démarrage
 *      si la règle n'est pas respectée.
 *   3. {@link #extractUsernameIgnoreExpiry} extrait le sujet même d'un token expiré,
 *      ce qui est nécessaire pour l'endpoint {@code POST /auth/refresh} (PROBLÈME 9).
 *
 * SÉCURITÉ :
 *   En production, injectez jwt.secret via une variable d'environnement ou un coffre
 *   (Vault, AWS Secrets Manager…) et ne le committez JAMAIS dans le dépôt Git.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /**
     * Longueur minimale du secret JWT (32 caractères = 256 bits).
     * HMAC-SHA256 requiert au minimum 256 bits de clé.
     */
    private static final int MIN_SECRET_LENGTH = 32;

    /**
     * Valeur par défaut connue du secret — présente dans application.properties.
     * À NE PAS utiliser en production. Le @PostConstruct la détecte et lève une exception.
     */
    private static final String DEFAULT_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private final JwtProperties jwtProperties;

    /**
     * Profil Spring actif — injecté pour distinguer "dev" des autres environnements.
     * En profil "dev", la valeur par défaut du secret est tolérée (avertissement seulement).
     */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FIX PROBLÈME 3 — Validation du secret au démarrage
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valide la configuration JWT au démarrage.
     *
     * Règles :
     *   - Le secret ne peut pas être null/vide.
     *   - Le secret doit faire au moins {@value MIN_SECRET_LENGTH} caractères.
     *   - En environnement non-dev, le secret par défaut est interdit.
     *
     * @throws IllegalStateException si une règle n'est pas respectée hors profil dev
     */
    @PostConstruct
    public void validateSecretOnStartup() {
        String secret = jwtProperties.getSecret();

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "[JWT] jwt.secret est null ou vide. Configurez une clé secrète d'au moins "
                    + MIN_SECRET_LENGTH + " caractères.");
        }

        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "[JWT] jwt.secret est trop court (" + secret.length() + " caractères). "
                    + "Minimum requis : " + MIN_SECRET_LENGTH + " caractères (256 bits).");
        }

        boolean isDefaultSecret = DEFAULT_SECRET.equals(secret);
        boolean isDevProfile    = "dev".equalsIgnoreCase(activeProfile);

        if (isDefaultSecret) {
            if (isDevProfile) {
                log.warn("[JWT] ⚠ ATTENTION : jwt.secret utilise la valeur par défaut du dépôt Git. "
                        + "Remplacez-la IMPÉRATIVEMENT avant tout déploiement en production.");
            } else {
                // Environnement non-dev avec le secret par défaut → crash immédiat
                throw new IllegalStateException(
                        "[JWT] jwt.secret utilise la valeur par défaut. "
                        + "Ce secret est public (dans le dépôt Git) et NE PEUT PAS être utilisé "
                        + "en environnement '" + activeProfile + "'. "
                        + "Définissez jwt.secret via une variable d'environnement sécurisée.");
            }
        }

        log.info("[JWT] Secret validé — longueur={} chars, profil={}", secret.length(), activeProfile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Token Generation
    // ─────────────────────────────────────────────────────────────────────────

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }

    private String buildToken(Map<String, Object> extraClaims,
                               UserDetails userDetails,
                               long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Token Validation
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Claims Extraction
    // ─────────────────────────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationMs() {
        return jwtProperties.getExpiration();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * FIX PROBLÈME 9 — Extrait le username même d'un token expiré.
     *
     * Utilisé par l'endpoint {@code POST /auth/refresh} : le token est expiré mais
     * la signature est valide → on peut faire confiance au subject extrait.
     *
     * @param token token JWT potentiellement expiré
     * @return username (subject) ou null si la signature est invalide
     */
    public String extractUsernameIgnoreExpiry(String token) {
        try {
            return extractUsername(token);
        } catch (ExpiredJwtException e) {
            // Token expiré mais signature valide → on retourne le subject
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.warn("[JWT] Token invalide (signature corrompue) lors de l'extraction du username : {}",
                    e.getMessage());
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
package com.cms.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.cms.config.properties.ApplicationProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Service technique de gestion des tokens JWT.
 *
 * <p>Conforme a la regle LOOP 3.8 : le token ne contient que
 * {@code sub} (= userId), {@code userId}, {@code iat} et {@code exp}.
 * Aucune permission, aucun profil, aucune liste d'acces n'y est integree :
 * les droits sont toujours relus en base (RBAC dynamique, LOOP 3.9).
 *
 * <p>Algorithme : HS256 (secret lu depuis {@code app.security.jwt.secret}).
 * Expiration : {@code app.security.jwt.expiration-ms}.
 */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long expirationMs;

    public JwtService(ApplicationProperties properties) {
        String secret = properties.getSecurity().getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "La propriete app.security.jwt.secret doit etre renseignee (variable JWT_SECRET)");
        }
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getSecurity().getJwt().getExpirationMs();
    }

    /**
     * Genere un token pour un utilisateur. Le sujet ({@code sub}) est
     * l'identifiant de l'utilisateur (regle LOOP 3.8).
     *
     * @param userId identifiant de l'utilisateur
     * @return token JWT signe
     */
    public String genererToken(Long userId) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cle)
                .compact();
    }

    /**
     * Verifie la signature et l'expiration du token.
     *
     * @param token token a verifier
     * @return {@code true} si le token est valide (signature et expiration)
     */
    public boolean estValide(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extrait l'identifiant de l'utilisateur du token.
     *
     * @param token token valide
     * @return identifiant de l'utilisateur
     */
    public Long extraireUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    Claims extraireClaims(String token) {
        return parseClaims(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(cle)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}

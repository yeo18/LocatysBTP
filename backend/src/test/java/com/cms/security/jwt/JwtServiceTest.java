package com.cms.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cms.config.properties.ApplicationProperties;

import io.jsonwebtoken.Claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires du service JWT : generation, lecture des claims,
 * validation (expiration, signature), respect de la regle LOOP 3.8
 * (token limite a sub, userId, iat, exp).
 */
class JwtServiceTest {

    private static final String SECRET = "cle-test-cms-2026-0123456789abcdefghijklmnopqrstu";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = creerService(3600000L);
    }

    private JwtService creerService(long expirationMs) {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getSecurity().getJwt().setSecret(SECRET);
        properties.getSecurity().getJwt().setExpirationMs(expirationMs);
        return new JwtService(properties);
    }

    @Test
    void genererToken_retourneTokenValideAvecClaimsAttendus() {
        String token = jwtService.genererToken(42L);

        assertThat(token).isNotBlank();
        assertThat(jwtService.estValide(token)).isTrue();
        assertThat(jwtService.extraireUserId(token)).isEqualTo(42L);
    }

    @Test
    void token_contientUniquementSubUserIdIatExp() {
        String token = jwtService.genererToken(42L);

        Claims claims = jwtService.extraireClaims(token);
        assertThat(claims.keySet()).containsExactlyInAnyOrder("sub", "userId", "iat", "exp");
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
    }

    @Test
    void tokenModifie_invalide() {
        String token = jwtService.genererToken(42L);
        String modifie = token.substring(0, token.length() - 4) + "AAAA";

        assertThat(jwtService.estValide(modifie)).isFalse();
    }

    @Test
    void tokenExpire_invalide() {
        JwtService serviceExpire = creerService(-1000L);
        String token = serviceExpire.genererToken(42L);

        assertThat(serviceExpire.estValide(token)).isFalse();
    }

    @Test
    void tokenSigneAvecAutreSecret_invalide() {
        JwtService autreService = creerService(3600000L);
        ApplicationProperties autres = new ApplicationProperties();
        autres.getSecurity().getJwt().setSecret("autre-secret-totalement-different-abcdefghijklmn");
        autres.getSecurity().getJwt().setExpirationMs(3600000L);
        JwtService signeAvecAutreCle = new JwtService(autres);
        String token = signeAvecAutreCle.genererToken(42L);

        assertThat(autreService.estValide(token)).isFalse();
    }

    @Test
    void tokenChaineVide_invalide() {
        assertThat(jwtService.estValide("")).isFalse();
        assertThat(jwtService.estValide("nimporte.quoi")).isFalse();
    }

    @Test
    void secretAbsent_leveExceptionAuDemarrage() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getSecurity().getJwt().setSecret("");

        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.jwt.secret");
    }
}

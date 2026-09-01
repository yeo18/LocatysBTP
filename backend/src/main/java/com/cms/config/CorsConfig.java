package com.cms.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration CORS pour la communication avec le frontend React.
 *
 * <p>Origines autorisées :
 * <ul>
 *   <li>la liste fixe {@code app.cors.allowed-origins} (dev :
 *       http://localhost:3000 ; prod : variable d'environnement
 *       CORS_ALLOWED_ORIGINS) ;</li>
 *   <li>toute origine du <b>réseau local</b> (IP privées RFC 1918 :
 *       10.x.x.x, 172.16-31.x.x, 192.168.x.x) — ainsi, si l'IP de la
 *       machine ou d'un appareil change, l'accès depuis le Wi-Fi/LAN
 *       continue de fonctionner sans réinstallation.</li>
 * </ul>
 * Le bean est automatiquement utilisé par Spring Security via
 * {@code http.cors(...)} (voir {@code SecurityConfig}).
 */
@Configuration
public class CorsConfig {

    /** Jokers d'origine pour le réseau local (IP privées uniquement, jamais un domaine public). */
    private static final List<String> JOKERS_RESEAU_LOCAL = List.of(
            "http://10.*:*",
            "https://10.*:*",
            "http://172.16.*:*", "http://172.17.*:*", "http://172.18.*:*", "http://172.19.*:*",
            "http://172.20.*:*", "http://172.21.*:*", "http://172.22.*:*", "http://172.23.*:*",
            "http://172.24.*:*", "http://172.25.*:*", "http://172.26.*:*", "http://172.27.*:*",
            "http://172.28.*:*", "http://172.29.*:*", "http://172.30.*:*", "http://172.31.*:*",
            "https://172.16.*:*", "https://172.17.*:*", "https://172.18.*:*", "https://172.19.*:*",
            "https://172.20.*:*", "https://172.21.*:*", "https://172.22.*:*", "https://172.23.*:*",
            "https://172.24.*:*", "https://172.25.*:*", "https://172.26.*:*", "https://172.27.*:*",
            "https://172.28.*:*", "https://172.29.*:*", "https://172.30.*:*", "https://172.31.*:*",
            "http://192.168.*:*",
            "https://192.168.*:*");

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(combinerOrigines());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Liste configurée + jokers réseau local (origine exacte et IP privées). */
    private List<String> combinerOrigines() {
        List<String> origines = new ArrayList<>();
        if (allowedOrigins != null) {
            origines.addAll(allowedOrigins);
        }
        origines.addAll(JOKERS_RESEAU_LOCAL);
        return origines;
    }
}

package com.cms.config;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {

    /**
     * Encodage BCrypt des mots de passe. Jamais stockés en clair.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Client HTTP utilisé par les clients d'APIs externes (Nominatim,
     * OpenWeather, Overpass).
     *
     * <p>Connect et read timeout volontairement courts : une API externe
     * indisponible ne doit pas bloquer l'analyse complète.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Pool de threads pour les appels parallèles des sources d'analyse
     * (météo, environnement).
     */
    @Bean
    public ExecutorService analyseExecutorService() {
        return Executors.newFixedThreadPool(4);
    }

}
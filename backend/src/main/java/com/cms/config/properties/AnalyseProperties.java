package com.cms.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Proprietes du module « Analyse du site ».
 *
 * Mapping yml :
 *   app.analyse.environnement.rayon-metres
 *   app.analyse.nominatim.user-agent
 *   app.analyse.openweather.api-key
 */
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.analyse")
public class AnalyseProperties {

    private Environnement environnement = new Environnement();
    private Nominatim nominatim = new Nominatim();
    private OpenWeather openweather = new OpenWeather();

    @Data
    public static class Environnement {
        /** Rayon de recherche Overpass autour du chantier (metres). */
        private int rayonMetres = 1000;
    }

    @Data
    public static class Nominatim {
        /** User-Agent identifiable obligatoire pour la politique d'usage. */
        private String userAgent = "BatiFlowCMS/1.0 (gestion de chantiers; contact: admin@batiflow.com)";
        /**
         * Code(s) pays ISO 3166-1 alpha-2 pour biaiser/restreindre la
         * recherche (ex : "ci" pour la Côte d'Ivoire). Vide = tous pays.
         */
        private String countryCodes = "ci";
        /** Nombre de résultats renvoyés par le géocodage (1-20). */
        private int limit = 8;
        /**
         * Cadre "minLon,minLat,maxLon,maxLat" utilisé comme zone de
         * proximité pour la recherche (défaut : bbox autour d'Abidjan).
         * Vide = pas de restriction géométrique.
         */
        private String viewbox = "-4.25,5.18,-3.55,5.55";
        /**
         * true = résultats LIMITÉS strictement à viewbox ; false (défaut) =
         * ses résultats sont simplement privilégiés (le reste de la CI reste
         * trouvable).
         */
        private boolean bounded = false;
    }

    /**
     * Propriétés OpenWeather (plan gratuit : conditions actuelles + prévisions
     * 5 jours / pas de 3 h). La clé API provient exclusivement de la variable
     * d'environnement {@code OPENWEATHER_API_KEY} — jamais stockée en clair ici.
     */
    @Data
    public static class OpenWeather {
        /** Clé API OpenWeather (plan gratuit). Obligatoire pour activer la météo. */
        private String apiKey = "";

        /** Unité métrique (SI) : °C, m/s, hPa. */
        private String units = "metric";

        /** Langue des descriptions météo renvoyées par l'API. */
        private String lang = "fr";

        /** Durée de vie du cache des réponses météo (secondes). */
        private long cacheSecondes = 1800;
    }

}
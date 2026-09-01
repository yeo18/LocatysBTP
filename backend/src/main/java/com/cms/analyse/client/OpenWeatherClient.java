package com.cms.analyse.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cms.analyse.cache.TtlCache;
import com.cms.analyse.dto.MeteoActuelleResponse;
import com.cms.analyse.dto.MeteoHoraireResponse;
import com.cms.analyse.dto.MeteoQuotidienneResponse;
import com.cms.analyse.dto.QualiteAirResponse;
import com.cms.config.properties.AnalyseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client HTTP de l'API OpenWeather (plan gratuit — aucune clé ne doit être
 * committée, elle arrive via la variable d'environnement
 * {@code OPENWEATHER_API_KEY}).
 *
 * <p>Deux endpoints gratuits sont utilisés :
 * <ul>
 *   <li>{@code /data/2.5/weather} : conditions actuelles ;</li>
 *   <li>{@code /data/2.5/forecast} : prévisions 5 jours à pas de 3 h
 *       (agrégées ici en prévisions quotidiennes).</li>
 * </ul>
 *
 * <p>Le client ne lance jamais d'exception vers l'orchestrateur : toute
 * erreur (clé absente, quota, indisponibilité, dépassement de délai) produit
 * un résultat vide, laissé à l'appelant pour un statut {@code INDISPONIBLE}
 * ou {@code ERREUR} sans faire échouer l'analyse complète.
 */
@Component
public class OpenWeatherClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherClient.class);

    private static final String URL_ACTUEL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String URL_PREVISION = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String URL_QUALITE_AIR = "https://api.openweathermap.org/data/2.5/air_pollution";
    private static final double M_S_EN_KM_H = 3.6;
    private static final DateTimeFormatter DT_TXT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TtlCache cache;
    private final AnalyseProperties properties;

    public OpenWeatherClient(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             TtlCache cache,
                             AnalyseProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.cache = cache;
        this.properties = properties;
    }

    /** Résultat météo complet (actuel + quotidiennes + horaires). */
    public record DonneesMeteo(MeteoActuelleResponse actuel,
                               List<MeteoQuotidienneResponse> quotidiennes,
                               List<MeteoHoraireResponse> horaires) {
    }

    /**
     * Indique si une clé API OpenWeather est configurée.
     *
     * @return {@code true} si la météo peut être interrogée
     */
    public boolean estConfigure() {
        return !properties.getOpenweather().getApiKey().isBlank();
    }

    /**
     * Récupère les données météo pour des coordonnées données.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @return données météo (actuel + quotidien 5 j + horaire), jamais
     *         {@code null} ; en cas d'échec l'actuel est {@code null} et les
     *         listes sont vides
     */
    public DonneesMeteo meteo(BigDecimal latitude, BigDecimal longitude) {
        if (!estConfigure()) {
            logger.warn("OpenWeather non configuré (OPENWEATHER_API_KEY manquante).");
            return new DonneesMeteo(null, List.of(), List.of());
        }

        String cleCache = "openweather:" + latitude + "," + longitude;
        DonneesMeteo enCache = cache.get(cleCache);
        if (enCache != null) {
            return enCache;
        }

        try {
            String urlActuel = URL_ACTUEL + parametres(latitude, longitude);
            String urlPrevision = URL_PREVISION + parametres(latitude, longitude);

            JsonNode actuelNode = objectMapper.readTree(restTemplate.getForObject(urlActuel, String.class));
            JsonNode previsionNode = objectMapper.readTree(restTemplate.getForObject(urlPrevision, String.class));

            DonneesMeteo donnees = new DonneesMeteo(
                    parserActuel(actuelNode),
                    parserQuotidiennes(previsionNode),
                    parserHoraires(previsionNode));
            cache.put(cleCache, donnees, properties.getOpenweather().getCacheSecondes());
            return donnees;
        } catch (Exception e) {
            logger.warn("Erreur OpenWeather {} ; {} : {}", latitude, longitude, e.getMessage());
            return new DonneesMeteo(null, List.of(), List.of());
        }
    }

    /**
     * Récupère la qualité de l'air au point donné (indice AQI + polluants).
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @return qualité de l'air, ou {@code null} si le service est non
     *         configuré, en erreur ou sans mesure
     */
    public QualiteAirResponse qualiteAir(BigDecimal latitude, BigDecimal longitude) {
        if (!estConfigure()) {
            logger.warn("OpenWeather non configuré (OPENWEATHER_API_KEY manquante).");
            return null;
        }

        String cleCache = "openweather-air:" + latitude + "," + longitude;
        QualiteAirResponse enCache = cache.get(cleCache);
        if (enCache != null) {
            return enCache;
        }

        try {
            String url = URL_QUALITE_AIR + parametres(latitude, longitude);
            JsonNode root = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            JsonNode liste = root.path("list");
            if (liste.isEmpty()) {
                return null;
            }
            JsonNode entree = liste.get(0);
            JsonNode main = entree.path("main");
            JsonNode composants = entree.path("components");

            QualiteAirResponse qualite = new QualiteAirResponse();
            qualite.setAqi(main.path("aqi").asInt(0));
            qualite.setLibelle(libelleAqi(main.path("aqi").asInt(0)));
            qualite.setPm25(composants.path("pm2_5").asDouble());
            qualite.setPm10(composants.path("pm10").asDouble());
            qualite.setO3(composants.path("o3").asDouble());
            qualite.setNo2(composants.path("no2").asDouble());
            qualite.setSo2(composants.path("so2").asDouble());
            qualite.setCo(composants.path("co").asDouble());
            qualite.setHeureMiseAJour(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(entree.path("dt").asLong(0)), ZoneOffset.UTC));
            cache.put(cleCache, qualite, properties.getOpenweather().getCacheSecondes());
            return qualite;
        } catch (Exception e) {
            logger.warn("Erreur qualite de l'air OpenWeather {} ; {} : {}", latitude, longitude, e.getMessage());
            return null;
        }
    }

    private String libelleAqi(int aqi) {
        return switch (aqi) {
            case 1 -> "Bon";
            case 2 -> "Correct";
            case 3 -> "Moyen";
            case 4 -> "Mauvais";
            case 5 -> "Très mauvais";
            default -> "Indéterminé";
        };
    }

    private String parametres(BigDecimal latitude, BigDecimal longitude) {
        AnalyseProperties.OpenWeather ow = properties.getOpenweather();
        return "?lat=" + latitude
                + "&lon=" + longitude
                + "&appid=" + ow.getApiKey()
                + "&units=" + ow.getUnits()
                + "&lang=" + ow.getLang();
    }

    // ------------------------------------------------------------------
    // Mapping — conditions actuelles
    // ------------------------------------------------------------------

    private MeteoActuelleResponse parserActuel(JsonNode root) {
        MeteoActuelleResponse actuel = new MeteoActuelleResponse();
        JsonNode main = root.path("main");
        JsonNode vent = root.path("wind");
        JsonNode nuages = root.path("clouds");
        JsonNode weather = premierWeather(root);

        actuel.setTemperature(main.path("temp").asDouble());
        actuel.setTemperatureRessentie(main.path("feels_like").asDouble());
        actuel.setHumidite(main.path("humidity").asDouble());
        actuel.setPression(main.path("pressure").asDouble());
        actuel.setPrecipitations(precipiteEnCours(root.path("rain")));
        actuel.setNeige(precipiteEnCours(root.path("snow")));
        actuel.setVent(vent.path("speed").asDouble() * M_S_EN_KM_H);
        actuel.setDirectionVent(vent.path("deg").asDouble());
        actuel.setRafales(vent.path("gust").asDouble() * M_S_EN_KM_H);
        actuel.setCouvertureNuageuse(nuages.path("all").asDouble());
        actuel.setVisibilite(root.path("visibility").asDouble());
        actuel.setConditions(weather.path("main").asText(""));
        actuel.setDescription(weather.path("description").asText(""));
        actuel.setCodeMeteo(weather.path("id").asInt(0));
        actuel.setHeureMiseAJour(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(root.path("dt").asLong(0)), ZoneOffset.UTC));
        return actuel;
    }

    // ------------------------------------------------------------------
    // Mapping — prévisions horaires (pas de 3 h)
    // ------------------------------------------------------------------

    private List<MeteoHoraireResponse> parserHoraires(JsonNode root) {
        List<MeteoHoraireResponse> horaires = new ArrayList<>();
        for (JsonNode entree : root.path("list")) {
            JsonNode main = entree.path("main");
            JsonNode vent = entree.path("wind");
            JsonNode weather = premierWeather(entree);

            MeteoHoraireResponse h = new MeteoHoraireResponse();
            h.setDateHeure(LocalDateTime.parse(entree.path("dt_txt").asText(), DT_TXT));
            h.setTemperature(main.path("temp").asDouble());
            h.setTemperatureRessentie(main.path("feels_like").asDouble());
            h.setPrecipitations(precipiteSurPas(entree));
            h.setProbaPrecipitations(entree.path("pop").asDouble(0) * 100);
            h.setVent(vent.path("speed").asDouble() * M_S_EN_KM_H);
            h.setHumidite(main.path("humidity").asDouble());
            h.setCouvertureNuageuse(entree.path("clouds").path("all").asDouble());
            h.setDescription(weather.path("description").asText(""));
            h.setCodeMeteo(weather.path("id").asInt(0));
            horaires.add(h);
        }
        return horaires;
    }

    // ------------------------------------------------------------------
    // Mapping — prévisions quotidiennes (agrégées sur 5 jours)
    // ------------------------------------------------------------------

    private List<MeteoQuotidienneResponse> parserQuotidiennes(JsonNode root) {
        Map<LocalDate, Jour> jours = new LinkedHashMap<>();
        for (JsonNode entree : root.path("list")) {
            LocalDate date = LocalDate.parse(entree.path("dt_txt").asText().substring(0, 10));
            JsonNode main = entree.path("main");
            JsonNode vent = entree.path("wind");
            jours.computeIfAbsent(date, Jour::new)
                    .accumuler(main.path("temp_max").asDouble(),
                            main.path("temp_min").asDouble(),
                            entree.path("pop").asDouble(0),
                            vent.path("speed").asDouble() * M_S_EN_KM_H,
                            vent.path("gust").asDouble() * M_S_EN_KM_H,
                            precipiteSurPas(entree),
                            main.path("humidity").asDouble(),
                            entree.path("clouds").path("all").asDouble(),
                            entree.path("dt_txt").asText(),
                            premierWeather(entree));
        }

        List<MeteoQuotidienneResponse> quotidiennes = new ArrayList<>();
        for (Jour jour : jours.values()) {
            MeteoQuotidienneResponse q = new MeteoQuotidienneResponse();
            q.setDate(jour.date());
            q.setTempMax(jour.tempMax());
            q.setTempMin(jour.tempMin());
            q.setPrecipitations(jour.precipitations());
            q.setProbaPrecipitations(jour.probaMax());
            q.setVentMax(jour.ventMax());
            q.setRafales(jour.rafalesMax());
            q.setHumiditeMax(jour.humiditeMax());
            q.setCouvertureNuageuse(jour.couvertureNuageuse());
            q.setDescription(jour.description());
            q.setCodeMeteo(jour.codeMeteo());
            quotidiennes.add(q);
        }
        return quotidiennes;
    }

    /** Agrégateur quotidien construit à partir des entrées à pas de 3 h. */
    private static final class Jour {
        private final LocalDate date;
        private double tempMax = Double.NEGATIVE_INFINITY;
        private double tempMin = Double.POSITIVE_INFINITY;
        private double precipitations;
        private double ventMax;
        private double rafalesMax;
        private double humiditeMax;
        private double sommeNuages;
        private int nbreEntrees;

        private String descMeilleureProba;
        private int codeMeilleureProba;
        private double meilleureProba;

        private String descMidi;
        private int codeMidi;
        private double distanceMidiMin = Double.POSITIVE_INFINITY;

        Jour(LocalDate date) {
            this.date = date;
        }

        LocalDate date() {
            return date;
        }

        double tempMax() {
            return arrondi(tempMax);
        }

        double tempMin() {
            return arrondi(tempMin);
        }

        double precipitations() {
            return arrondi(precipitations);
        }

        double probaMax() {
            return arrondi(meilleureProba * 100);
        }

        double ventMax() {
            return arrondi(ventMax);
        }

        double rafalesMax() {
            return arrondi(rafalesMax);
        }

        double humiditeMax() {
            return arrondi(humiditeMax);
        }

        /** Couverture nuageuse moyenne du jour (%). */
        double couvertureNuageuse() {
            return nbreEntrees == 0 ? 0 : arrondi(sommeNuages / nbreEntrees);
        }

        /** Condition la plus représentative : celle associée à la probabilité
         * de précipitations maximale, sinon la condition de mi-journée. */
        String description() {
            return meilleureProba > 0 ? (descMeilleureProba == null ? "" : descMeilleureProba)
                    : (descMidi == null ? "" : descMidi);
        }

        int codeMeteo() {
            return meilleureProba > 0 ? codeMeilleureProba : codeMidi;
        }

        void accumuler(double tempMax, double tempMin, double pop, double vent, double rafale,
                       double precip, double humidite, double nuages, String dtTxt, JsonNode weather) {
            this.tempMax = Math.max(this.tempMax, tempMax);
            this.tempMin = Math.min(this.tempMin, tempMin);
            this.precipitations += precip;
            this.ventMax = Math.max(this.ventMax, vent);
            this.rafalesMax = Math.max(this.rafalesMax, rafale);
            this.humiditeMax = Math.max(this.humiditeMax, humidite);
            this.sommeNuages += nuages;
            this.nbreEntrees++;

            if (pop > meilleureProba) {
                meilleureProba = pop;
                descMeilleureProba = weather.path("description").asText("");
                codeMeilleureProba = weather.path("id").asInt(0);
            }

            int heure = Integer.parseInt(dtTxt.substring(11, 13));
            double distanceMidi = Math.abs(heure - 12);
            if (distanceMidi < distanceMidiMin) {
                distanceMidiMin = distanceMidi;
                descMidi = weather.path("description").asText("");
                codeMidi = weather.path("id").asInt(0);
            }
        }
    }

    private static double arrondi(double valeur) {
        return Math.round(valeur * 10) / 10.0;
    }

    private JsonNode premierWeather(JsonNode node) {
        JsonNode weather = node.path("weather");
        return weather.isEmpty() ? weather : weather.get(0);
    }

    private double precipiteEnCours(JsonNode objet) {
        return objet.path("1h").asDouble(objet.path("3h").asDouble(0));
    }

    private double precipiteSurPas(JsonNode entree) {
        return entree.path("rain").path("3h").asDouble(0)
                + entree.path("snow").path("3h").asDouble(0);
    }

}
package com.cms.analyse.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.cms.analyse.cache.TtlCache;
import com.cms.analyse.dto.GeocodageResultatResponse;
import com.cms.config.properties.AnalyseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client HTTP du service de géocodage Nominatim (OpenStreetMap).
 *
 * <p>Respecte la politique d'usage du serveur public : une requête sur
 * action explicite uniquement (pas d'autocomplétion), User-Agent
 * identifiable, attribution OSM, cache des résultats (24 h). Résultats
 * limités aux premières réponses pertinentes.
 */
@Component
public class NominatimClient {

    private static final Logger logger = LoggerFactory.getLogger(NominatimClient.class);
    private static final long CACHE_DUREE_SECON = 24 * 3600;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AnalyseProperties properties;
    private final TtlCache cache;

    public NominatimClient(RestTemplate restTemplate,
                           ObjectMapper objectMapper,
                           AnalyseProperties properties,
                           TtlCache cache) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.cache = cache;
    }

    /**
     * Recherche une position à partir d'une description libre (ex :
     * « Près du CHU de Cocody »). Retourne une liste de positions
     * proposées (jamais confirmées ici).
     *
     * <p>Stratégie de précision : recherche principale restreinte aux pays
     * configurés (Côte d'Ivoire par défaut) + zone de proximité Abidjan.
     * Si elle ne trouve rien, un second essai sans filtre pays/zone est
     * tenté (lieu hors CI) pour ne jamais renvoyer vide si le lieu existe.
     *
     * @param description description du lieu / adresse approximative
     * @return positions proposées (au plus limit configuré)
     */
    public List<GeocodageResultatResponse> geocoder(String description) {
        String cleCache = "nominatim:q:" + description.trim().toLowerCase();
        List<GeocodageResultatResponse> enCache = cache.get(cleCache);
        if (enCache != null) {
            return enCache;
        }

        List<GeocodageResultatResponse> resultats = rechercher(description, true);
        if (resultats.isEmpty()
                && !description.isBlank()
                && !contientPaysExplicite(description)) {
            logger.info("Geocodage principal sans résultat pour '{}' — essai sans filtre pays/zone", description);
            resultats = rechercher(description, false);
        }
        cache.put(cleCache, resultats, CACHE_DUREE_SECON);
        return resultats;
    }

    private List<GeocodageResultatResponse> rechercher(String description, boolean filtreLocal) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("q", description)
                .queryParam("format", "jsonv2")
                .queryParam("limit", properties.getNominatim().getLimit())
                .queryParam("addressdetails", 1)
                .queryParam("extratags", 1)
                .queryParam("namedetails", 1)
                .queryParam("accept-language", "fr");
        if (filtreLocal) {
            String pays = properties.getNominatim().getCountryCodes();
            if (pays != null && !pays.isBlank()) {
                builder.queryParam("countrycodes", pays);
            }
            String viewbox = properties.getNominatim().getViewbox();
            if (viewbox != null && !viewbox.isBlank()) {
                builder.queryParam("viewbox", viewbox);
                if (properties.getNominatim().isBounded()) {
                    builder.queryParam("bounded", 1);
                }
            }
        }
        String url = builder.build().toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class,
                    entetesNominatim());
            return parser(body);
        } catch (Exception e) {
            logger.warn("Erreur geocodage Nominatim pour '{}' : {}", description, e.getMessage());
            return List.of();
        }
    }

    /**
     * Évite le fallback quand la description mentionne déjà un pays :
     * l'utilisateur a volontairement cherché ailleurs que la CI.
     */
    private boolean contientPaysExplicite(String description) {
        String d = description.toLowerCase();
        for (String pays : List.of("cote d'ivoire", "côte d'ivoire", "cote divoire",
                "ivory coast", "france", "bénin", "benin", "sénégal", "senegal",
                "burkina", "mali", "nigeria", "ghana", "togo", "guinée", "guinee")) {
            if (d.contains(pays)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Détermine la localisation humaine (ville, commune, quartier, pays)
     * d'un point (reverse géocodage).
     */
    public LocalisationHumaineReverse reverse(BigDecimal latitude, BigDecimal longitude) {
        String cleCache = "nominatim:reverse:" + latitude + "," + longitude;
        LocalisationHumaineReverse enCache = cache.get(cleCache);
        if (enCache != null) {
            return enCache;
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", 1)
                .queryParam("accept-language", "fr")
                .build()
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class, entetesNominatim());
            JsonNode root = objectMapper.readTree(body);
            JsonNode address = root.path("address");
            LocalisationHumaineReverse resultat = new LocalisationHumaineReverse(
                    valeurOuVide(address, "city"),
                    valeurOuVide(address, "town"),
                    valeurOuVide(address, "village"),
                    valeurOuVide(address, "county"),
                    valeurOuVide(address, "state"),
                    valeurOuVide(address, "country"));
            cache.put(cleCache, resultat, CACHE_DUREE_SECON);
            return resultat;
        } catch (Exception e) {
            logger.warn("Erreur reverse Nominatim {} ; {} : {}", latitude, longitude, e.getMessage());
            return new LocalisationHumaineReverse("", "", "", "", "", "");
        }
    }

    private List<GeocodageResultatResponse> parser(String body) throws Exception {
        List<GeocodageResultatResponse> resultats = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return resultats;
        }
        JsonNode root = objectMapper.readTree(body);
        for (JsonNode n : root) {
            GeocodageResultatResponse r = new GeocodageResultatResponse();
            r.setLibelle(n.path("display_name").asText(""));
            r.setLatitude(new BigDecimal(n.path("lat").asText("0")));
            r.setLongitude(new BigDecimal(n.path("lon").asText("0")));
            r.setType(n.path("type").asText(""));
            JsonNode address = n.path("address");
            r.setVille(valeurOuVide(address, "city"));
            r.setCommune(communefrom(address));
            r.setQuartier(quartierFrom(address));
            r.setPays(valeurOuVide(address, "country"));
            resultats.add(r);
        }
        return resultats;
    }

    private String valeurOuVide(JsonNode address, String champ) {
        JsonNode v = address.path(champ);
        return v.isMissingNode() ? "" : v.asText();
    }

    private String quartierFrom(JsonNode address) {
        for (String champ : List.of("neighbourhood", "suburb", "quarter", "residential", "city_district")) {
            String v = valeurOuVide(address, champ);
            if (!v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String communefrom(JsonNode address) {
        for (String champ : List.of("town", "city_district", "borough", "village", "county")) {
            String v = valeurOuVide(address, champ);
            if (!v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String[] entetesNominatim() {
        return new String[]{"user-agent", properties.getNominatim().getUserAgent()};
    }

    /** Localisation humaine d'un point (résultat du reverse géocodage). */
    public record LocalisationHumaineReverse(String ville, String villeProche, String village,
                                             String departement, String region, String pays) {

        /**
         * Construit une chaine de mots-clés géographiques hiérarchisés
         * (ex : « Cocody Abidjan Cote d'Ivoire »).
         */
        public String motsCles() {
            StringBuilder sb = new StringBuilder();
            ajouter(sb, ville);
            ajouter(sb, villeProche);
            ajouter(sb, village);
            ajouter(sb, departement);
            ajouter(sb, region);
            ajouter(sb, pays);
            return sb.toString().trim();
        }

        private void ajouter(StringBuilder sb, String valeur) {
            if (valeur != null && !valeur.isBlank() && sb.indexOf(valeur) == -1) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(valeur);
            }
        }
    }
}

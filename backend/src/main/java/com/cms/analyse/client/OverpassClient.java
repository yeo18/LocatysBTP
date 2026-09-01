package com.cms.analyse.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cms.analyse.cache.TtlCache;
import com.cms.analyse.dto.ElementEnvironnementResponse;
import com.cms.config.properties.AnalyseProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client HTTP de l'API Overpass (lecture OpenStreetMap) : recherche des
 * points d'intérêt autour du chantier dans un rayon paramétrable.
 */
@Component
public class OverpassClient {

    private static final Logger logger = LoggerFactory.getLogger(OverpassClient.class);
    private static final long CACHE_DUREE_SECON = 12 * 3600; // 12 h

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AnalyseProperties properties;
    private final TtlCache cache;

    public OverpassClient(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          AnalyseProperties properties,
                          TtlCache cache) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.cache = cache;
    }

    /**
     * Recherche les éléments d'environnement (routes, écoles, hôpitaux,
     * cours d'eau, stations-service, bâtiments, zones résidentielles,
     * points d'intérêt) autour d'un point dans un rayon donné.
     *
     * @param latitude   latitude du chantier
     * @param longitude  longitude du chantier
     * @param rayonMetre rayon de recherche (mètres)
     * @return éléments trouvés, triés par distance croissante (au plus 60)
     */
    public List<ElementEnvironnementResponse> rechercher(BigDecimal latitude, BigDecimal longitude, int rayonMetre) {
        String cleCache = "overpass:" + latitude + "," + longitude + ":" + rayonMetre;
        List<ElementEnvironnementResponse> enCache = cache.get(cleCache);
        if (enCache != null) {
            return enCache;
        }

        String query = construireRequete(latitude, longitude, rayonMetre);
        try {
            HttpHeaders entetes = new HttpHeaders();
            entetes.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> requete = new HttpEntity<>(query, entetes);

            String body = restTemplate.postForObject(
                    "https://overpass-api.de/api/interpreter", requete, String.class);
            List<ElementEnvironnementResponse> elements = parser(body, latitude, longitude);
            cache.put(cleCache, elements, CACHE_DUREE_SECON);
            return elements;
        } catch (Exception e) {
            logger.warn("Erreur Overpass {} ; {} : {}", latitude, longitude, e.getMessage());
            return List.of();
        }
    }

    private String construireRequete(BigDecimal latitude, BigDecimal longitude, int rayonMetre) {
        String autour = rayonMetre + "," + latitude + "," + longitude;
        return "[out:json][timeout:25];("
                + "nwr[\"amenity\"~\"^(school|hospital|clinic|fuel|place_of_worship|fire_station|police)$\"](around:" + autour + ");"
                + "nwr[\"waterway\"~\"^(river|stream|canal)$\"](around:" + autour + ");"
                + "nwr[\"highway\"~\"^(trunk|primary|secondary)$\"](around:" + autour + ");"
                + "nwr[\"landuse\"~\"^(residential|industrial|construction|commercial)$\"](around:" + autour + ");"
                + "nwr[\"building\"](around:" + autour + ");"
                + "nwr[\"tourism\"](around:" + autour + ");"
                + "nwr[\"leisure\"](around:" + autour + ");"
                + "nwr[\"shop\"](around:" + autour + ");"
                + ");out center 100;";
    }

    private List<ElementEnvironnementResponse> parser(String body, BigDecimal latitude, BigDecimal longitude)
            throws Exception {
        List<ElementEnvironnementResponse> elements = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return elements;
        }
        JsonNode root = objectMapper.readTree(body);
        JsonNode elementsNode = root.path("elements");
        double latCentre = latitude.doubleValue();
        double lonCentre = longitude.doubleValue();
        for (JsonNode n : elementsNode) {
            double lat;
            double lon;
            JsonNode centre = n.path("center");
            if (!centre.isMissingNode()) {
                lat = centre.path("lat").asDouble();
                lon = centre.path("lon").asDouble();
            } else {
                lat = n.path("lat").asDouble();
                lon = n.path("lon").asDouble();
            }
            if (lat == 0 && lon == 0) {
                continue;
            }
            double distanceKm = distanceKm(latCentre, lonCentre, lat, lon);
            String nom = n.path("tags").path("name").asText("");
            String type = determinerType(n);
            if (nom.isBlank()) {
                nom = type;
            }
            elements.add(new ElementEnvironnementResponse(nom, type, lat, lon, Math.round(distanceKm * 1000.0) / 1000.0));
        }
        elements.sort(Comparator.comparingDouble(ElementEnvironnementResponse::getDistanceKm));
        return elements.size() > 60 ? elements.subList(0, 60) : elements;
    }

    private String determinerType(JsonNode n) {
        JsonNode tags = n.path("tags");
        if (tags.has("amenity")) {
            return "amenity_" + tags.path("amenity").asText();
        }
        if (tags.has("waterway")) {
            return "cours_d_eau";
        }
        if (tags.has("highway")) {
            return "route";
        }
        if (tags.has("landuse")) {
            return "zone_" + tags.path("landuse").asText();
        }
        if (tags.has("building")) {
            return "batiment";
        }
        if (tags.has("shop")) {
            return "commerce";
        }
        if (tags.has("leisure")) {
            return "loisir";
        }
        if (tags.has("tourism")) {
            return "tourisme";
        }
        return "point_d_interet";
    }

    /** Distance haversine en kilomètres entre deux points géographiques. */
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double rayon = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * rayon * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

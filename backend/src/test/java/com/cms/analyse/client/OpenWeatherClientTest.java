package com.cms.analyse.client;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.cms.analyse.cache.TtlCache;
import com.cms.analyse.dto.MeteoActuelleResponse;
import com.cms.analyse.dto.MeteoHoraireResponse;
import com.cms.analyse.dto.MeteoQuotidienneResponse;
import com.cms.analyse.dto.QualiteAirResponse;
import com.cms.config.properties.AnalyseProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests unitaires du client OpenWeather (plan gratuit).
 * Aucun appel réseau réel : MockRestServiceServer remplace l'API.
 */
class OpenWeatherClientTest {

    private static final BigDecimal LAT = new BigDecimal("5.3");
    private static final BigDecimal LON = new BigDecimal("-4.0");
    private static final String CLE = "CLE_TEST_NON_COMMITTEE";

    private static final String URL_ACTUEL =
            "https://api.openweathermap.org/data/2.5/weather?lat=5.3&lon=-4.0&appid="
                    + CLE + "&units=metric&lang=fr";
    private static final String URL_PREVISION =
            "https://api.openweathermap.org/data/2.5/forecast?lat=5.3&lon=-4.0&appid="
                    + CLE + "&units=metric&lang=fr";
    private static final String URL_AIR =
            "https://api.openweathermap.org/data/2.5/air_pollution?lat=5.3&lon=-4.0&appid="
                    + CLE + "&units=metric&lang=fr";

    private static final String JSON_ACTUEL = """
            {
              "dt": 1755660000,
              "main": {"temp": 28.5, "feels_like": 31.2, "humidity": 78, "pressure": 1012},
              "visibility": 10000,
              "wind": {"speed": 3.2, "deg": 200, "gust": 7.4},
              "clouds": {"all": 20},
              "weather": [{"id": 800, "main": "Clear", "description": "ciel degage"}]
            }
            """;

    private static final String JSON_ACTUEL_PLUIE = """
            {
              "dt": 1755660000,
              "main": {"temp": 24.0, "feels_like": 26.0, "humidity": 90, "pressure": 1008},
              "visibility": 5000,
              "wind": {"speed": 1.5, "deg": 300},
              "clouds": {"all": 80},
              "rain": {"1h": 0.2},
              "weather": [{"id": 500, "main": "Rain", "description": "pluie legere"}]
            }
            """;

    private static final String JSON_PREVISION = """
            {
              "list": [
                {"dt":1,"main":{"temp":20,"feels_like":19,"temp_min":18,"temp_max":24,"humidity":90},"wind":{"speed":2,"deg":90,"gust":4},"clouds":{"all":40},"pop":0.8,"rain":{"3h":1.2},"weather":[{"id":500,"main":"Rain","description":"pluie legere"}],"dt_txt":"2026-08-20 00:00:00"},
                {"dt":2,"main":{"temp":21,"feels_like":20,"temp_min":18,"temp_max":25,"humidity":85},"wind":{"speed":3,"deg":90},"clouds":{"all":60},"pop":0.5,"weather":[{"id":803,"main":"Clouds","description":"nuageux"}],"dt_txt":"2026-08-20 03:00:00"},
                {"dt":3,"main":{"temp":22,"feels_like":21,"temp_min":20,"temp_max":20,"humidity":80},"wind":{"speed":1,"deg":90,"gust":2},"clouds":{"all":20},"pop":0.1,"weather":[{"id":800,"main":"Clear","description":"ciel degage"}],"dt_txt":"2026-08-20 06:00:00"},
                {"dt":4,"main":{"temp":23,"feels_like":22,"temp_min":19,"temp_max":27,"humidity":95},"wind":{"speed":4,"deg":180},"clouds":{"all":80},"pop":0.7,"rain":{"3h":0.4},"weather":[{"id":501,"main":"Rain","description":"pluie moderee"}],"dt_txt":"2026-08-21 00:00:00"}
              ]
            }
            """;

    private static final String JSON_AIR = """
            {
              "coord": {"lat": 5.3, "lon": -4.0},
              "list": [
                {"main": {"aqi": 4}, "components": {"co": 422.5, "no": 0.02, "no2": 12.7, "o3": 38.9, "so2": 3.1, "pm2_5": 38.4, "pm10": 66.2, "nh3": 1.2}, "dt": 1755660000}
              ]
            }
            """;

    private static final String JSON_AIR_VIDE = """
            {"coord": {"lat": 5.3, "lon": -4.0}, "list": []}
            """;

    private AnalyseProperties properties;
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private TtlCache cache;
    private OpenWeatherClient client;

    @BeforeEach
    void setUp() {
        properties = new AnalyseProperties();
        properties.getOpenweather().setApiKey(CLE);
        properties.getOpenweather().setUnits("metric");
        properties.getOpenweather().setLang("fr");
        properties.getOpenweather().setCacheSecondes(1800);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        cache = new TtlCache();
        client = new OpenWeatherClient(restTemplate, objectMapper, cache, properties);
    }

    @Test
    void meteo_coordonneesValides_retourneActuelEtPrevisions() {
        mockServer.expect(requestTo(URL_ACTUEL))
                .andRespond(withSuccess(JSON_ACTUEL, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(URL_PREVISION))
                .andRespond(withSuccess(JSON_PREVISION, MediaType.APPLICATION_JSON));

        OpenWeatherClient.DonneesMeteo donnees = client.meteo(LAT, LON);

        // Actuel
        MeteoActuelleResponse actuel = donnees.actuel();
        assertThat(actuel).isNotNull();
        assertThat(actuel.getTemperature()).isEqualTo(28.5);
        assertThat(actuel.getTemperatureRessentie()).isEqualTo(31.2);
        assertThat(actuel.getHumidite()).isEqualTo(78);
        assertThat(actuel.getPression()).isEqualTo(1012);
        assertThat(actuel.getPrecipitations()).isZero();
        assertThat(actuel.getNeige()).isZero();
        assertThat(actuel.getVent()).isCloseTo(11.5, within(0.05)); // 3.2 m/s -> km/h
        assertThat(actuel.getDirectionVent()).isEqualTo(200);
        assertThat(actuel.getRafales()).isCloseTo(26.6, within(0.05)); // 7.4 m/s -> km/h
        assertThat(actuel.getCouvertureNuageuse()).isEqualTo(20);
        assertThat(actuel.getVisibilite()).isEqualTo(10000);
        assertThat(actuel.getConditions()).isEqualTo("Clear");
        assertThat(actuel.getDescription()).isEqualTo("ciel degage");
        assertThat(actuel.getCodeMeteo()).isEqualTo(800);
        assertThat(actuel.getHeureMiseAJour()).isNotNull();

        // Prévisions
        assertThat(donnees.horaires()).hasSize(4);
        assertThat(donnees.quotidiennes()).hasSize(2);
        mockServer.verify();
    }

    @Test
    void meteo_avecPluie_remplitPrecipitations() {
        mockServer.expect(requestTo(URL_ACTUEL))
                .andRespond(withSuccess(JSON_ACTUEL_PLUIE, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(URL_PREVISION))
                .andRespond(withSuccess(JSON_PREVISION, MediaType.APPLICATION_JSON));

        MeteoActuelleResponse actuel = client.meteo(LAT, LON).actuel();

        assertThat(actuel.getPrecipitations()).isEqualTo(0.2);
        assertThat(actuel.getDescription()).isEqualTo("pluie legere");
        assertThat(actuel.getCodeMeteo()).isEqualTo(500);
    }

    @Test
    void meteo_sansPluie_precipitationsNulles() {
        mockServer.expect(requestTo(URL_ACTUEL))
                .andRespond(withSuccess(JSON_ACTUEL, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(URL_PREVISION))
                .andRespond(withSuccess(JSON_PREVISION, MediaType.APPLICATION_JSON));

        MeteoActuelleResponse actuel = client.meteo(LAT, LON).actuel();

        assertThat(actuel.getPrecipitations()).isZero();
        assertThat(actuel.getNeige()).isZero();
    }

    @Test
    void prevision_transformee_enQuotidiennesEtHoraires() {
        mockServer.expect(requestTo(URL_ACTUEL))
                .andRespond(withSuccess(JSON_ACTUEL, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(URL_PREVISION))
                .andRespond(withSuccess(JSON_PREVISION, MediaType.APPLICATION_JSON));

        OpenWeatherClient.DonneesMeteo donnees = client.meteo(LAT, LON);

        MeteoHoraireResponse h0 = donnees.horaires().get(0);
        assertThat(h0.getDateHeure()).isEqualTo(java.time.LocalDateTime.of(2026, 8, 20, 0, 0));
        assertThat(h0.getTemperature()).isEqualTo(20);
        assertThat(h0.getProbaPrecipitations()).isEqualTo(80);
        assertThat(h0.getPrecipitations()).isCloseTo(1.2, within(0.001));
        assertThat(h0.getVent()).isCloseTo(7.2, within(0.05)); // 2 m/s -> km/h
        assertThat(h0.getHumidite()).isEqualTo(90);
        assertThat(h0.getCouvertureNuageuse()).isEqualTo(40);
        assertThat(h0.getDescription()).isEqualTo("pluie legere");

        MeteoQuotidienneResponse jour1 = donnees.quotidiennes().get(0);
        assertThat(jour1.getDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 20));
        assertThat(jour1.getTempMax()).isEqualTo(25);
        assertThat(jour1.getTempMin()).isEqualTo(18);
        assertThat(jour1.getProbaPrecipitations()).isEqualTo(80);
        assertThat(jour1.getPrecipitations()).isCloseTo(1.2, within(0.001));
        assertThat(jour1.getVentMax()).isCloseTo(10.8, within(0.05)); // 3 m/s -> km/h
        assertThat(jour1.getRafales()).isCloseTo(14.4, within(0.05)); // 4 m/s -> km/h
        assertThat(jour1.getHumiditeMax()).isEqualTo(90);
        assertThat(jour1.getCouvertureNuageuse()).isEqualTo(40); // moyenne (40+60+20)/3
        assertThat(jour1.getDescription()).isEqualTo("pluie legere"); // représentation = proba max
        assertThat(jour1.getCodeMeteo()).isEqualTo(500);

        MeteoQuotidienneResponse jour2 = donnees.quotidiennes().get(1);
        assertThat(jour2.getDate()).isEqualTo(java.time.LocalDate.of(2026, 8, 21));
        assertThat(jour2.getTempMax()).isEqualTo(27);
        assertThat(jour2.getProbaPrecipitations()).isEqualTo(70);
        assertThat(jour2.getHumiditeMax()).isEqualTo(95);
        assertThat(jour2.getCouvertureNuageuse()).isEqualTo(80);
        assertThat(jour2.getDescription()).isEqualTo("pluie moderee");
    }

    @Test
    void meteo_sansCleConfiguree_retourneVideSansAppelHttp() {
        properties.getOpenweather().setApiKey("");

        OpenWeatherClient.DonneesMeteo donnees = client.meteo(LAT, LON);

        assertThat(client.estConfigure()).isFalse();
        assertThat(donnees.actuel()).isNull();
        assertThat(donnees.quotidiennes()).isEmpty();
        assertThat(donnees.horaires()).isEmpty();
        mockServer.verify(); // aucun appel réseau attendu
    }

    @Test
    void meteo_cleInvalide_401_retourneVideSansException() {
        mockServer.expect(requestTo(URL_ACTUEL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        OpenWeatherClient.DonneesMeteo donnees = client.meteo(LAT, LON);

        assertThat(donnees.actuel()).isNull();
        assertThat(donnees.quotidiennes()).isEmpty();
        assertThat(donnees.horaires()).isEmpty();
    }

    @Test
    void meteo_apiIndisponible_erreurServeur_retourneVideSansException() {
        mockServer.expect(requestTo(URL_ACTUEL)).andRespond(withServerError());

        OpenWeatherClient.DonneesMeteo donnees = client.meteo(LAT, LON);

        assertThat(donnees.actuel()).isNull();
        assertThat(donnees.quotidiennes()).isEmpty();
        assertThat(donnees.horaires()).isEmpty();
    }

    @Test
    void qualiteAir_disponible_parseIndiceEtPolluants() {
        mockServer.expect(requestTo(URL_AIR))
                .andRespond(withSuccess(JSON_AIR, MediaType.APPLICATION_JSON));

        QualiteAirResponse qualite = client.qualiteAir(LAT, LON);

        assertThat(qualite).isNotNull();
        assertThat(qualite.getAqi()).isEqualTo(4);
        assertThat(qualite.getLibelle()).isEqualTo("Mauvais");
        assertThat(qualite.getPm25()).isCloseTo(38.4, within(0.001));
        assertThat(qualite.getPm10()).isCloseTo(66.2, within(0.001));
        assertThat(qualite.getO3()).isCloseTo(38.9, within(0.001));
        assertThat(qualite.getNo2()).isCloseTo(12.7, within(0.001));
        assertThat(qualite.getSo2()).isCloseTo(3.1, within(0.001));
        assertThat(qualite.getCo()).isCloseTo(422.5, within(0.001));
        assertThat(qualite.getHeureMiseAJour()).isNotNull();
        mockServer.verify();
    }

    @Test
    void qualiteAir_sansCleConfiguree_retourneNull() {
        properties.getOpenweather().setApiKey("");

        assertThat(client.qualiteAir(LAT, LON)).isNull();
        mockServer.verify(); // aucun appel réseau
    }

    @Test
    void qualiteAir_aucuneMesure_listeVide_retourneNull() {
        mockServer.expect(requestTo(URL_AIR))
                .andRespond(withSuccess(JSON_AIR_VIDE, MediaType.APPLICATION_JSON));

        assertThat(client.qualiteAir(LAT, LON)).isNull();
    }

    @Test
    void qualiteAir_apiEnErreur_retourneNullSansException() {
        mockServer.expect(requestTo(URL_AIR)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThat(client.qualiteAir(LAT, LON)).isNull();
    }

    private static org.assertj.core.data.Offset<Double> within(double valeur) {
        return org.assertj.core.data.Offset.offset(valeur);
    }

}
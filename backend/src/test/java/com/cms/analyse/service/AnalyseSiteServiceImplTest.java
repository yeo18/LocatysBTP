package com.cms.analyse.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.analyse.client.NominatimClient;
import com.cms.analyse.client.OpenWeatherClient;
import com.cms.analyse.client.OpenWeatherClient.DonneesMeteo;
import com.cms.analyse.client.OverpassClient;
import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.ElementEnvironnementResponse;
import com.cms.analyse.dto.MeteoActuelleResponse;
import com.cms.analyse.dto.QualiteAirResponse;
import com.cms.analyse.dto.SectionEnvironnementResponse;
import com.cms.analyse.dto.SectionMeteoResponse;
import com.cms.analyse.dto.StatutSource;
import com.cms.analyse.dto.SyntheseResponse;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.config.properties.AnalyseProperties;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de l'orchestration de l'analyse du site : une source en
 * échec (météo OpenWeather) ne doit jamais faire échouer l'analyse complète.
 */
@ExtendWith(MockitoExtension.class)
class AnalyseSiteServiceImplTest {

    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private DataAccessService dataAccessService;
    @Mock
    private NominatimClient nominatimClient;
    @Mock
    private OpenWeatherClient openWeatherClient;
    @Mock
    private OverpassClient overpassClient;
    @Mock
    private SyntheseService syntheseService;

    private AnalyseProperties properties;
    private ExecutorService executor;
    private AnalyseSiteServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new AnalyseProperties();
        executor = Executors.newFixedThreadPool(4);
        service = new AnalyseSiteServiceImpl(chantierRepository, dataAccessService,
                nominatimClient, openWeatherClient, overpassClient,
                syntheseService, properties, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private Chantier chantierAvecCoordonnees() {
        Chantier chantier = new Chantier();
        chantier.setId(42L);
        chantier.setLatitude(new BigDecimal("5.33"));
        chantier.setLongitude(new BigDecimal("-4.02"));
        return chantier;
    }

    private void sourcesDisponibles() {
        when(overpassClient.rechercher(any(), any(), any(Integer.class)))
                .thenReturn(List.of(new ElementEnvironnementResponse("Ecole Cocody", "amenity_school", 5.33, -4.02, 0.5)));
    }

    @Test
    void analyser_coordonneesValides_toutesSourcesDisponibles() {
        Chantier chantier = chantierAvecCoordonnees();
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantier));
        when(openWeatherClient.estConfigure()).thenReturn(true);
        when(openWeatherClient.meteo(any(), any()))
                .thenReturn(new DonneesMeteo(new MeteoActuelleResponse(), List.of(), List.of()));
        QualiteAirResponse qualite = new QualiteAirResponse();
        qualite.setAqi(2);
        qualite.setLibelle("Correct");
        when(openWeatherClient.qualiteAir(any(), any())).thenReturn(qualite);
        sourcesDisponibles();
        when(syntheseService.synthetiser(any(AnalyseSiteResponse.class)))
                .thenReturn(new SyntheseResponse("LEGERE", List.of(), List.of(), List.of(), "ok"));

        AnalyseSiteResponse resultat = service.analyser(42L);

        assertThat(resultat.getChantierId()).isEqualTo(42L);
        assertThat(resultat.getLatitude()).isEqualByComparingTo("5.33");
        assertThat(resultat.getLongitude()).isEqualByComparingTo("-4.02");
        assertThat(resultat.getMeteo().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
        assertThat(resultat.getMeteo().getQualiteAir()).isNotNull();
        assertThat(resultat.getMeteo().getQualiteAir().getAqi()).isEqualTo(2);
        assertThat(resultat.getEnvironnement().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
        assertThat(resultat.getSynthese()).isNotNull();
        verify(syntheseService).synthetiser(any(AnalyseSiteResponse.class));
    }

    @Test
    void analyser_sansCleApi_meteoIndisponible_analyseContinue() {
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantierAvecCoordonnees()));
        when(openWeatherClient.estConfigure()).thenReturn(false);
        sourcesDisponibles();

        AnalyseSiteResponse resultat = service.analyser(42L);

        assertThat(resultat.getMeteo().getStatut()).isEqualTo(StatutSource.INDISPONIBLE);
        assertThat(resultat.getMeteo().getMessage()).contains("OPENWEATHER_API_KEY");
        assertThat(resultat.getEnvironnement().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
    }

    @Test
    void analyser_meteoIndisponibleErreurApi_analyseContinue() {
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantierAvecCoordonnees()));
        when(openWeatherClient.estConfigure()).thenReturn(true);
        when(openWeatherClient.meteo(any(), any()))
                .thenReturn(new DonneesMeteo(null, List.of(), List.of()));
        sourcesDisponibles();

        AnalyseSiteResponse resultat = service.analyser(42L);

        SectionMeteoResponse meteo = resultat.getMeteo();
        assertThat(meteo.getStatut()).isEqualTo(StatutSource.INDISPONIBLE);
        assertThat(meteo.getActuel()).isNull();
        assertThat(resultat.getEnvironnement().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
    }

    @Test
    void analyser_qualiteAirIndisponible_meteoResteDisponible() {
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantierAvecCoordonnees()));
        when(openWeatherClient.estConfigure()).thenReturn(true);
        when(openWeatherClient.meteo(any(), any()))
                .thenReturn(new DonneesMeteo(new MeteoActuelleResponse(), List.of(), List.of()));
        when(openWeatherClient.qualiteAir(any(), any())).thenReturn(null);
        sourcesDisponibles();

        AnalyseSiteResponse resultat = service.analyser(42L);

        assertThat(resultat.getMeteo().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
        assertThat(resultat.getMeteo().getQualiteAir()).isNull();
        assertThat(resultat.getEnvironnement().getStatut()).isEqualTo(StatutSource.DISPONIBLE);
    }

    @Test
    void analyser_erreurInattendueMeteo_sectionErreur_analyseContinue() {
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantierAvecCoordonnees()));
        when(openWeatherClient.estConfigure()).thenReturn(true);
        when(openWeatherClient.meteo(any(), any())).thenThrow(new RuntimeException("boom"));
        sourcesDisponibles();

        AnalyseSiteResponse resultat = service.analyser(42L);

        assertThat(resultat.getMeteo().getStatut()).isEqualTo(StatutSource.ERREUR);
        assertThat(resultat.getEnvironnement()).isNotNull();
    }

    @Test
    void analyser_chantierSansCoordonnees_leveBadRequest() {
        Chantier chantier = new Chantier();
        chantier.setId(42L);
        when(chantierRepository.findById(42L)).thenReturn(Optional.of(chantier));

        assertThatThrownBy(() -> service.analyser(42L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("position confirmée");
    }

    @Test
    void analyser_chantierIntrouvable_leveResourceNotFound() {
        when(chantierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.analyser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void geocoder_delegueAuClientNominatim() {
        when(nominatimClient.geocoder("Cocody")).thenReturn(List.of());

        assertThat(service.geocoder("Cocody")).isEmpty();
        verify(nominatimClient).geocoder("Cocody");
    }

    @Test
    void geocoder_descriptionVide_leveBadRequest() {
        assertThatThrownBy(() -> service.geocoder("   "))
                .isInstanceOf(BadRequestException.class);
    }

}
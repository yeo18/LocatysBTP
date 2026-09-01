package com.cms.analyse.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.analyse.client.NominatimClient;
import com.cms.analyse.client.OpenWeatherClient;
import com.cms.analyse.client.OpenWeatherClient.DonneesMeteo;
import com.cms.analyse.client.OverpassClient;
import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.ElementEnvironnementResponse;
import com.cms.analyse.dto.GeocodageResultatResponse;
import com.cms.analyse.dto.SectionEnvironnementResponse;
import com.cms.analyse.dto.SectionMeteoResponse;
import com.cms.analyse.dto.QualiteAirResponse;
import com.cms.analyse.dto.StatutSource;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.config.properties.AnalyseProperties;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

/**
 * Orchestration de l'analyse du site : les sources indépendantes (météo,
 * environnement) sont appelées en parallèle pour réduire le temps
 * d'attente. Une source en échec produit une section {@code ERREUR} ou
 * {@code INDISPONIBLE} sans bloquer les autres.
 */
@Service
public class AnalyseSiteServiceImpl implements AnalyseSiteService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseSiteServiceImpl.class);

    private final ChantierRepository chantierRepository;
    private final DataAccessService dataAccessService;
    private final NominatimClient nominatimClient;
    private final OpenWeatherClient openWeatherClient;
    private final OverpassClient overpassClient;
    private final SyntheseService syntheseService;
    private final AnalyseProperties properties;
    private final ExecutorService executor;

    public AnalyseSiteServiceImpl(ChantierRepository chantierRepository,
                                  DataAccessService dataAccessService,
                                  NominatimClient nominatimClient,
                                  OpenWeatherClient openWeatherClient,
                                  OverpassClient overpassClient,
                                  SyntheseService syntheseService,
                                  AnalyseProperties properties,
                                  ExecutorService executor) {
        this.chantierRepository = chantierRepository;
        this.dataAccessService = dataAccessService;
        this.nominatimClient = nominatimClient;
        this.openWeatherClient = openWeatherClient;
        this.overpassClient = overpassClient;
        this.syntheseService = syntheseService;
        this.properties = properties;
        this.executor = executor;
    }

    @Override
    public List<GeocodageResultatResponse> geocoder(String description) {
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Description de localisation requise.");
        }
        return nominatimClient.geocoder(description);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyseSiteResponse analyser(Long chantierId) {
        Chantier chantier = trouverChantier(chantierId);
        dataAccessService.verifierAccesChantier(chantierId);

        BigDecimal latitude = chantier.getLatitude();
        BigDecimal longitude = chantier.getLongitude();
        if (latitude == null || longitude == null) {
            throw new BadRequestException(
                    "Aucune position confirmée pour ce chantier. Localisez et confirmez la position avant l'analyse.");
        }

        AnalyseSiteResponse analyse = new AnalyseSiteResponse();
        analyse.setChantierId(chantierId);
        analyse.setLatitude(latitude);
        analyse.setLongitude(longitude);

        CompletableFuture<SectionMeteoResponse> meteo = CompletableFuture
                .supplyAsync(() -> analyserMeteo(latitude, longitude), executor);
        CompletableFuture<SectionEnvironnementResponse> environnement = CompletableFuture
                .supplyAsync(() -> analyserEnvironnement(latitude, longitude), executor);

        analyse.setMeteo(meteo.join());
        analyse.setEnvironnement(environnement.join());

        analyse.setSynthese(syntheseService.synthetiser(analyse));

        logger.info("Analyse site chantier {} terminee (meteo={}, env={})",
                chantierId, analyse.getMeteo().getStatut(), analyse.getEnvironnement().getStatut());
        return analyse;
    }

    // ------------------------------------------------------------------
    // Sources individuelles (chaque source est isolée)
    // ------------------------------------------------------------------

    private SectionMeteoResponse analyserMeteo(BigDecimal latitude, BigDecimal longitude) {
        try {
            if (!openWeatherClient.estConfigure()) {
                return new SectionMeteoResponse(StatutSource.INDISPONIBLE,
                        "Accès météo non configuré (variable OPENWEATHER_API_KEY manquante).",
                        null, List.of(), List.of(), null);
            }
            DonneesMeteo donnees = openWeatherClient.meteo(latitude, longitude);
            if (donnees.actuel() == null) {
                return new SectionMeteoResponse(StatutSource.INDISPONIBLE,
                        "Aucune donnée météo disponible pour cette position.",
                        null, List.of(), List.of(), null);
            }
            QualiteAirResponse qualiteAir = lireQualiteAir(latitude, longitude);
            return new SectionMeteoResponse(StatutSource.DISPONIBLE, null,
                    donnees.actuel(), donnees.quotidiennes(), donnees.horaires(), qualiteAir);
        } catch (Exception e) {
            logger.warn("Section meteo en echec : {}", e.getMessage());
            return new SectionMeteoResponse(StatutSource.ERREUR,
                    "La source météo est temporairement indisponible.",
                    null, List.of(), List.of(), null);
        }
    }

    /** La qualité de l'air est complémentaire : son échec n'altère pas la météo. */
    private QualiteAirResponse lireQualiteAir(BigDecimal latitude, BigDecimal longitude) {
        try {
            return openWeatherClient.qualiteAir(latitude, longitude);
        } catch (Exception e) {
            logger.warn("Qualite de l'air indisponible : {}", e.getMessage());
            return null;
        }
    }

    private SectionEnvironnementResponse analyserEnvironnement(BigDecimal latitude, BigDecimal longitude) {
        int rayon = properties.getEnvironnement().getRayonMetres();
        try {
            List<ElementEnvironnementResponse> elements = overpassClient.rechercher(latitude, longitude, rayon);
            if (elements.isEmpty()) {
                return new SectionEnvironnementResponse(StatutSource.INDISPONIBLE,
                        "Aucun point d'intérêt trouvé dans le rayon de " + rayon + " m.", rayon, List.of());
            }
            return new SectionEnvironnementResponse(StatutSource.DISPONIBLE, null, rayon, elements);
        } catch (Exception e) {
            logger.warn("Section environnement en echec : {}", e.getMessage());
            return new SectionEnvironnementResponse(StatutSource.ERREUR,
                    "La source environnementale est temporairement indisponible.", rayon, List.of());
        }
    }

    private Chantier trouverChantier(Long chantierId) {
        return chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + chantierId));
    }
}
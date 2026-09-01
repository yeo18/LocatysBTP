package com.cms.analyse.service;

import java.util.List;

import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.GeocodageResultatResponse;

/**
 * Service métier du module « Analyse du site ».
 *
 * <p>Géocodage (Nominatim), analyse parallèle des sources (météo,
 * environnement) puis synthèse. Chaque source est
 * indépendante : une source indisponible ne fait jamais échouer l'analyse.
 */
public interface AnalyseSiteService {

    /**
     * Recherche une position à partir d'une description libre (géocodage).
     * Les résultats sont des propositions : la position n'est confirmée que
     * par l'utilisateur via ChantierService.confirmerLocalisation.
     *
     * @param description description du lieu / adresse approximative
     * @return positions proposées
     */
    List<GeocodageResultatResponse> geocoder(String description);

    /**
     * Analyse un chantier à partir de sa position confirmée. Sans position
     * confirmée, aucune analyse n'est exécutée (BadRequestException).
     *
     * @param chantierId identifiant du chantier
     * @return résultat complet de l'analyse (toutes sections)
     */
    AnalyseSiteResponse analyser(Long chantierId);

}

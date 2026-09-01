package com.cms.chantier.service;

import org.springframework.data.domain.Page;

import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.ChantierResumeResponse;
import com.cms.chantier.dto.ConfirmerLocalisationRequest;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.dto.UpdateChantierRequest;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.common.dto.SearchRequest;

/**
 * Service metier du module chantier.
 *
 * <p>Gestion du cycle de vie des chantiers (creation, modification,
 * consultation, annulation). La progression d'un chantier est calculee
 * depuis ses taches (jamais saisie manuellement).
 */
public interface ChantierService {

    /**
     * Cree un chantier (statut PREVU par defaut, progression 0).
     *
     * @param request donnees de creation
     * @return chantier cree
     */
    ChantierResponse creer(CreateChantierRequest request);

    /**
     * Modifie les informations d'un chantier.
     *
     * @param id      identifiant du chantier
     * @param request donnees de modification
     * @return chantier modifie
     */
    ChantierResponse modifier(Long id, UpdateChantierRequest request);

    /**
     * Annule un chantier (suppression logique : statut ANNULE).
     *
     * @param id identifiant du chantier
     * @return chantier annule
     */
    ChantierResponse annuler(Long id);

    /**
     * Consulte un chantier par identifiant.
     *
     * @param id identifiant du chantier
     * @return chantier
     */
    ChantierResponse trouverParId(Long id);

    /**
     * Recherche paginee de chantiers, avec filtre statut optionnel et
     * mot-cle sur nom/adresse.
     *
     * @param search criteres de pagination/recherche
     * @param statut filtre optionnel par statut
     * @return page de chantiers
     */
    Page<ChantierResumeResponse> rechercher(SearchRequest search, ChantierStatut statut);

    /**
     * Confirme la position gÃ©ographique d'un chantier (geocodage valide ou
     * correction manuelle sur la carte). La position confirmÃ©e devient les
     * coordonnees officielles utilisees pour l'analyse du site.
     *
     * @param id      identifiant du chantier
     * @param request latitude/longitude + metadonnees de localisation
     * @return chantier mis a jour
     */
    ChantierResponse confirmerLocalisation(Long id, ConfirmerLocalisationRequest request);

}

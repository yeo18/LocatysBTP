package com.cms.tache.service;

import java.util.List;

import com.cms.tache.dto.AffectationTacheResponse;
import com.cms.tache.dto.CreateAffectationTacheRequest;
import com.cms.tache.dto.UpdateAffectationTacheRequest;

/**
 * Service metier des affectations de taches (LOOP 3.15).
 *
 * <p>Regle : au moins une cible obligatoire (utilisateur OU equipe).
 * Roles uniquement REALISATEUR / CONTROLEUR (enum {@code AffectationTacheRole}).
 */
public interface AffectationTacheService {

    /**
     * Affecte une tache a un utilisateur ou a une equipe.
     *
     * @param request tache + cible (utilisateur ou equipe) + role
     * @return affectation creee
     */
    AffectationTacheResponse assignTache(CreateAffectationTacheRequest request);

    /**
     * Modifie une affectation (cible ou role).
     *
     * @param id      identifiant de l'affectation
     * @param request donnees de modification
     * @return affectation modifiee
     */
    AffectationTacheResponse updateAffectation(Long id, UpdateAffectationTacheRequest request);

    /**
     * Retire une affectation.
     *
     * @param id identifiant de l'affectation
     */
    void removeAffectation(Long id);

    /**
     * Liste les affectations d'une tache (utilisateur et/ou equipe).
     *
     * @param tacheId identifiant de la tache
     * @return affectations de la tache
     */
    List<AffectationTacheResponse> findByTacheId(Long tacheId);
}

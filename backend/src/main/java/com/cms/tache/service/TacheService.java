package com.cms.tache.service;

import org.springframework.data.domain.Pageable;

import com.cms.common.response.PageResponse;
import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.TacheResumeResponse;
import com.cms.tache.dto.UpdateTacheRequest;

/**
 * Service metier du module tache (LOOP 3.15).
 *
 * <p>Gestion du cycle de vie d'une tache. Toute tache appartient a un
 * chantier (obligatoire). Le rattachement d'une equipe/equipe se fait via
 * le service d'affectation, la decision de validation via le service de
 * validation (historique conserve).
 */
public interface TacheService {

    /**
     * Cree une tache (statut initial A_FAIRE, progression 0).
     *
     * @param request donnees valides (titre, description, priorite, dates, chantier)
     * @return tache creee
     */
    TacheResponse createTache(CreateTacheRequest request);

    /**
     * Modifie une tache (titre, description, priorite, statut, progression, dates).
     *
     * @param id      identifiant de la tache
     * @param request donnees de modification
     * @return tache modifiee
     */
    TacheResponse updateTache(Long id, UpdateTacheRequest request);

    /**
     * Consulte une tache par identifiant (securite par donnees appliquee).
     *
     * @param id identifiant de la tache
     * @return tache
     */
    TacheResponse findById(Long id);

    /**
     * Liste paginee des taches accessibles a l'utilisateur courant
     * (securite par donnees appliquee).
     *
     * @param pageable pagination
     * @return page de taches resume
     */
    PageResponse<TacheResumeResponse> findAll(Pageable pageable);

    /**
     * Supprime une tache.
     *
     * @param id identifiant de la tache
     */
    void deleteTache(Long id);
}

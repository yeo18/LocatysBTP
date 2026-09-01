package com.cms.tache.service;

import java.util.List;

import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.ValidationTacheResponse;

/**
 * Service metier des validations de taches (LOOP 3.15).
 *
 * <p>Chaque decision (VALIDE / REFUSE) est une nouvelle ligne : l'historique
 * est conserve, jamais remplace par un simple champ dans {@code Tache}.
 */
public interface ValidationTacheService {

    /**
     * Enregistre une decision de validation d'une tache (historique conserve).
     *
     * @param request tache + statut (VALIDE/REFUSE) + commentaire + date
     * @return validation enregistree
     */
    ValidationTacheResponse validateTache(CreateValidationTacheRequest request);

    /**
     * Consulte l'historique des validations d'une tache.
     *
     * @param tacheId identifiant de la tache
     * @return historique des validations
     */
    List<ValidationTacheResponse> getValidationHistory(Long tacheId);
}

package com.cms.chantier.service;

import java.util.List;

import com.cms.chantier.dto.UtilisateurPermissionChantierResponse;

/**
 * Gestion des exceptions RBAC scopées à un chantier
 * ({@code utilisateur_permission_chantier}).
 */
public interface UtilisateurPermissionChantierService {

    /**
     * Accorde une permission à un utilisateur dans le périmètre du chantier.
     *
     * @param chantierId       identifiant du chantier
     * @param utilisateurId    identifiant de l'utilisateur
     * @param permissionId     identifiant de la permission
     * @return l'exception scopée créée
     */
    UtilisateurPermissionChantierResponse accorder(Long chantierId, Long utilisateurId, Long permissionId);

    /**
     * Refuse (retranche) une permission à un utilisateur dans le périmètre du
     * chantier. REFUSER est prioritaire sur le profil.
     */
    UtilisateurPermissionChantierResponse refuser(Long chantierId, Long utilisateurId, Long permissionId);

    /**
     * Retire une exception scopée existante.
     */
    void retirer(Long exceptionId);

    /**
     * Liste toutes les exceptions scopées d'un chantier.
     */
    List<UtilisateurPermissionChantierResponse> listerParChantier(Long chantierId);
}
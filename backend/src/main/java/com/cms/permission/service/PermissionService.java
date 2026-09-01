package com.cms.permission.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cms.common.dto.SearchRequest;
import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.dto.PermissionResumeResponse;
import com.cms.permission.dto.UpdatePermissionRequest;

/**
 * Service metier du referentiel des permissions (RBAC).
 *
 * <p>Le referentiel est quasi-statique : les permissions sont creees et
 * maintenues par l'administration. L'attribution aux profils et aux
 * utilisateurs est geree par {@code ProfilService} et
 * {@code UtilisateurService}.
 */
public interface PermissionService {

    /**
     * Cree une permission (nom technique unique, MAJUSCULES/underscore).
     *
     * @param request donnees de creation
     * @return permission creee
     */
    PermissionResponse creerPermission(CreatePermissionRequest request);

    /**
     * Modifie une permission existante.
     *
     * @param id      identifiant de la permission
     * @param request donnees de modification
     * @return permission modifiee
     */
    PermissionResponse modifierPermission(Long id, UpdatePermissionRequest request);

    /**
     * Supprime definitivement une permission (et ses associations
     * profil_permission / utilisateur_permission).
     *
     * @param id identifiant de la permission
     */
    void supprimerPermission(Long id);

    /**
     * Desactive une permission : retire tous ses octrois (profils et
     * utilisateurs) sans supprimer le referentiel. L'activation d'une
     * permission correspond a une re-attribution (voir ProfilService /
     * UtilisateurService).
     *
     * @param id identifiant de la permission
     * @return permission (referentiel conserve)
     */
    PermissionResponse desactiverPermission(Long id);

    /**
     * Reactive une permission : le referentiel etant permanent, l'activation
     * rend a nouveau la permission attribuable (a re-attribuer via
     * ProfilService.ajouterPermission / UtilisateurService.accorderPermission).
     *
     * @param id identifiant de la permission
     * @return permission activee
     */
    PermissionResponse activerPermission(Long id);

    /**
     * Recherche une permission par identifiant.
     *
     * @param id identifiant de la permission
     * @return permission
     */
    PermissionResponse trouverParId(Long id);

    /**
     * Recherche une permission par son nom technique.
     *
     * @param nomPermission nom technique (ex : TACHE_VIEW)
     * @return permission
     */
    PermissionResponse trouverParNomPermission(String nomPermission);

    /**
     * Liste les permissions d'un module.
     *
     * @param module nom du module (ex : TACHE)
     * @return liste des permissions du module
     */
    List<PermissionResumeResponse> listerParModule(String module);

    /**
     * Recherche paginee de permissions (mot-cle sur nom, nomPermission, module).
     *
     * @param search criteres de recherche
     * @return page de permissions
     */
    Page<PermissionResumeResponse> rechercher(SearchRequest search);

}

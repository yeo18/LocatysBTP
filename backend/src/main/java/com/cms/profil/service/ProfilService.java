package com.cms.profil.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cms.common.dto.SearchRequest;
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilPermissionResponse;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.dto.ProfilResumeResponse;
import com.cms.profil.dto.UpdateProfilRequest;

/**
 * Service metier des profils (roles RBAC) et de leurs permissions par defaut.
 *
 * <p>Regles : nom de profil unique ; profils systeme (ADMINISTRATEUR,
 * UTILISATEUR_STANDARD) proteges (non supprimables, non renommables).
 */
public interface ProfilService {

    /**
     * Cree un profil.
     *
     * @param request donnees de creation
     * @return profil cree
     */
    ProfilResponse creerProfil(CreateProfilRequest request);

    /**
     * Modifie un profil (sauf les profils systeme dont le nom est protege).
     *
     * @param id      identifiant du profil
     * @param request donnees de modification
     * @return profil modifie
     */
    ProfilResponse modifierProfil(Long id, UpdateProfilRequest request);

    /**
     * Supprime un profil (interdit pour les profils systeme et pour les profils
     * encore attribues a des utilisateurs).
     *
     * @param id identifiant du profil
     */
    void supprimerProfil(Long id);

    /**
     * Recherche un profil par identifiant.
     *
     * @param id identifiant du profil
     * @return profil
     */
    ProfilResponse trouverParId(Long id);

    /**
     * Recherche paginee de profils (mot-cle sur nom, description).
     *
     * @param search criteres de recherche
     * @return page de profils
     */
    Page<ProfilResumeResponse> rechercher(SearchRequest search);

    /**
     * Attribue une permission a un profil (association unique).
     *
     * @param profilId     identifiant du profil
     * @param permissionId identifiant de la permission
     * @return association creee
     */
    ProfilPermissionResponse ajouterPermission(Long profilId, Long permissionId);

    /**
     * Retire une permission d'un profil.
     *
     * @param profilId     identifiant du profil
     * @param permissionId identifiant de la permission
     */
    void retirerPermission(Long profilId, Long permissionId);

    /**
     * Liste les permissions attribuees a un profil.
     *
     * @param profilId identifiant du profil
     * @return liste des associations
     */
    List<ProfilPermissionResponse> listerPermissions(Long profilId);

}

package com.cms.utilisateur.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cms.common.dto.SearchRequest;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurPermissionResponse;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;

/**
 * Service metier du module utilisateur.
 *
 * <p>Responsabilites : inscription, consultation, modification, gestion des
 * permissions individuelles (RBAC, LOOP ultérieur). Aucune logique JWT ici.
 */
public interface UtilisateurService {

    /**
     * Cree un utilisateur (inscription).
     *
     * @param request donnees de creation
     * @return utilisateur cree
     */
    UtilisateurResponse creerUtilisateur(CreateUtilisateurRequest request);

    /**
     * Modifie les informations autorisees d'un utilisateur (nom, prenom,
     * telephone, etc.). Le changement de role/profil n'est pas permis ici.
     *
     * @param id      identifiant de l'utilisateur
     * @param request donnees de modification
     * @return utilisateur modifie
     */
    UtilisateurResponse modifierUtilisateur(Long id, UpdateUtilisateurRequest request);

    /**
     * Modifie le profil (role) d'un utilisateur existant.
     *
     * <p>Operation administrative protegee ({@code UTILISATEUR_MODIFIER}) :
     * elle ne s'applique jamais au flux public {@code POST /auth/register}
     * qui force toujours {@link com.cms.common.constants.SystemRoles#UTILISATEUR_STANDARD}.
     *
     * @param id       identifiant de l'utilisateur
     * @param profilId nouveau profil a attribuer (doit exister)
     * @return utilisateur modifie
     */
    UtilisateurResponse modifierProfil(Long id, Long profilId);

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return utilisateur
     */
    UtilisateurResponse trouverParId(Long id);

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email email de l'utilisateur
     * @return utilisateur
     */
    UtilisateurResponse trouverParEmail(String email);

    /**
     * Liste tous les utilisateurs.
     *
     * @return liste des utilisateurs
     */
    List<UtilisateurResumeResponse> trouverTous();

    /**
     * Recherche paginee d'utilisateurs (mot-cle, tri).
     *
     * @param search criteres de recherche
     * @return page d'utilisateurs
     */
    Page<UtilisateurResumeResponse> rechercher(SearchRequest search);

    /**
     * Octroie individuellement une permission a un utilisateur (exception RBAC
     * ACCORDER). Ne modifie pas le profil.
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @param permissionId  identifiant de la permission
     * @return exception creee
     */
    UtilisateurPermissionResponse accorderPermission(Long utilisateurId, Long permissionId);

    /**
     * Refuse individuellement une permission a un utilisateur (exception RBAC
     * REFUSER, prioritaire sur le profil et sur ACCORDER).
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @param permissionId  identifiant de la permission
     * @return exception creee
     */
    UtilisateurPermissionResponse refuserPermission(Long utilisateurId, Long permissionId);

    /**
     * Retire une exception RBAC individuelle (ACCORDER ou REFUSER).
     *
     * @param id identifiant de l'exception
     */
    void retirerPermissionIndividuelle(Long id);

    /**
     * Liste les exceptions RBAC individuelles d'un utilisateur.
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @return liste des exceptions
     */
    List<UtilisateurPermissionResponse> listerPermissionsIndividuelles(Long utilisateurId);

}

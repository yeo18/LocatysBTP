package com.cms.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cms.exception.custom.UnauthorizedException;
import com.cms.utilisateur.entity.Utilisateur;

/**
 * Helper de securite : expose l'utilisateur authentifie de la requete courante.
 *
 * <p>Utilise par les Services pour le controle de perimetre (Niveau 2, RBAC) :
 * l'utilisateur courant est recupere via le contexte de securite, jamais via
 * un parametre frontend.
 */
@Service
public class CurrentUserService {

    /**
     * Retourne l'entite utilisateur authentifiee.
     *
     * @return utilisateur courant
     * @throws UnauthorizedException si aucun utilisateur n'est authentifie
     */
    public Utilisateur getCurrentUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalUtilisateur principal)) {
            throw UnauthorizedException.generic();
        }
        return principal.getUtilisateur();
    }

    /**
     * Retourne l'identifiant de l'utilisateur authentifie.
     *
     * @return identifiant de l'utilisateur courant
     * @throws UnauthorizedException si aucun utilisateur n'est authentifie
     */
    public Long getCurrentUserId() {
        return getCurrentUtilisateur().getId();
    }

}

package com.cms.auth.service;

import com.cms.auth.dto.ChangerMotDePasseRequest;
import com.cms.auth.dto.LoginRequest;
import com.cms.auth.dto.MeResponse;
import com.cms.auth.dto.TokenResponse;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;

/**
 * Service d'authentification : inscription, connexion et profil courant.
 *
 * <p>L'inscription delegue la creation du compte a
 * {@code UtilisateurService}. La connexion verifie le couple email / mot de
 * passe (BCrypt via {@code AuthenticationManager}) puis emet un token JWT.
 */
public interface AuthenticationService {

    /**
     * Inscription : cree un compte utilisateur.
     *
     * @param request donnees de creation
     * @return utilisateur cree
     */
    UtilisateurResponse inscrire(CreateUtilisateurRequest request);

    /**
     * Connexion : verifie les identifiants et emet un token JWT.
     *
     * @param request email + mot de passe
     * @return token JWT + utilisateur authentifie
     */
    TokenResponse connexion(LoginRequest request);

    /**
     * Profil courant : utilisateur authentifie + droits effectifs.
     *
     * @return utilisateur et ses codes de permissions effectifs
     */
    MeResponse me();

    /**
     * Change le mot de passe de l'utilisateur courant (ancien mot de passe requis).
     *
     * @param request ancien + nouveau mot de passe + confirmation
     */
    void changerMotDePasse(ChangerMotDePasseRequest request);

}

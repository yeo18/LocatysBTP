package com.cms.auth.dto;

import com.cms.utilisateur.dto.UtilisateurResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse de connexion : token JWT + informations de l'utilisateur authentifie.
 *
 * <p>Le token ne contient jamais les permissions (regle LOOP 3.8) : les droits
 * effectifs sont relus en base a chaque requete (RBAC dynamique).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String token;
    private String type;
    private long expiresIn;
    private UtilisateurResponse utilisateur;
}

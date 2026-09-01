package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'un utilisateur n'existe pas.
 */
public class UtilisateurNonTrouveException extends CmsException {

    public UtilisateurNonTrouveException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public static UtilisateurNonTrouveException pourId(Long id) {
        return new UtilisateurNonTrouveException("Utilisateur introuvable avec l'identifiant : " + id);
    }

    public static UtilisateurNonTrouveException pourEmail(String email) {
        return new UtilisateurNonTrouveException("Utilisateur introuvable avec l'email : " + email);
    }

}

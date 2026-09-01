package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'un profil n'existe pas.
 */
public class ProfilIntrouvableException extends CmsException {

    public ProfilIntrouvableException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public static ProfilIntrouvableException pourId(Long id) {
        return new ProfilIntrouvableException("Profil introuvable avec l'identifiant : " + id);
    }

    public static ProfilIntrouvableException pourNom(String nom) {
        return new ProfilIntrouvableException("Profil introuvable avec le nom : " + nom);
    }

}

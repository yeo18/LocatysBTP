package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'un nom de profil existe deja (unicite).
 */
public class NomProfilDejaExistantException extends CmsException {

    public NomProfilDejaExistantException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public static NomProfilDejaExistantException pourNom(String nom) {
        return new NomProfilDejaExistantException("Le nom de profil existe deja : " + nom);
    }

}

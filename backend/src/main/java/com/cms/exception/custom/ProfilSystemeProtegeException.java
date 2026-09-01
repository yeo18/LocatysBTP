package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lors d'une operation interdite sur un profil systeme
 * (ADMINISTRATEUR, UTILISATEUR_STANDARD).
 */
public class ProfilSystemeProtegeException extends CmsException {

    public ProfilSystemeProtegeException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public static ProfilSystemeProtegeException nonSupprimable(String nom) {
        return new ProfilSystemeProtegeException(
                "Le profil systeme ne peut pas etre supprime : " + nom);
    }

    public static ProfilSystemeProtegeException nonRenommable(String nom) {
        return new ProfilSystemeProtegeException(
                "Le nom d'un profil systeme ne peut pas etre modifie : " + nom);
    }

    public static ProfilSystemeProtegeException nonDesactivable(String nom) {
        return new ProfilSystemeProtegeException(
                "Le profil systeme ne peut pas etre desactive : " + nom);
    }

}

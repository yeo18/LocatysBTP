package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception generique de doublon de ressource (conflit).
 *
 * <p>Levee lorsqu'une ressource de meme nature existe deja (email, code,
 * nom, association unique...) — HTTP 409.
 */
public class DuplicateResourceException extends CmsException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    /**
     * Fabrique un message clair pour un doublon.
     *
     * @param champ  champ en conflit (ex : email)
     * @param valeur valeur deja utilisee
     * @return exception de conflit
     */
    public static DuplicateResourceException pourConflit(String champ, String valeur) {
        return new DuplicateResourceException(champ + " deja utilise : " + valeur);
    }

}

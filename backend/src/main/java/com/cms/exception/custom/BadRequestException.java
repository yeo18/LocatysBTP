package com.cms.exception.custom;

import com.cms.common.constants.Messages;
import org.springframework.http.HttpStatus;

/**
 * Exception technique generique (non metier).
 */
public class BadRequestException extends CmsException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }

    public static BadRequestException generic() {
        return new BadRequestException(Messages.ERREUR_GENERIQUE);
    }

}

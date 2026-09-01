package com.cms.exception.custom;

import com.cms.common.constants.Messages;
import org.springframework.http.HttpStatus;

/**
 * Exception technique generique (non metier).
 */
public class UnauthorizedException extends CmsException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED.value());
    }

    public static UnauthorizedException generic() {
        return new UnauthorizedException(Messages.AUTHENTIFICATION_REQUISE);
    }

}

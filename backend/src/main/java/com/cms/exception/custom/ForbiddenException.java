package com.cms.exception.custom;

import com.cms.common.constants.Messages;
import org.springframework.http.HttpStatus;

/**
 * Exception technique generique (non metier).
 */
public class ForbiddenException extends CmsException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }

    public static ForbiddenException generic() {
        return new ForbiddenException(Messages.ACCES_REFUSE);
    }

}

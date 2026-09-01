package com.cms.exception.custom;

import com.cms.common.constants.Messages;
import org.springframework.http.HttpStatus;

/**
 * Exception technique generique (non metier).
 */
public class ResourceNotFoundException extends CmsException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public static ResourceNotFoundException generic() {
        return new ResourceNotFoundException(Messages.RESSOURCE_INTROUVABLE);
    }

}

package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lors d'un doublon d'email.
 */
public class EmailDejaUtiliseException extends CmsException {

    public EmailDejaUtiliseException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public static EmailDejaUtiliseException pourEmail(String email) {
        return new EmailDejaUtiliseException("L'email est deja utilise : " + email);
    }

}

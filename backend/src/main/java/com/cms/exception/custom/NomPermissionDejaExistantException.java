package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'un nom permission existe deja (unicite).
 */
public class NomPermissionDejaExistantException extends CmsException {

    public NomPermissionDejaExistantException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public static NomPermissionDejaExistantException pourNomPermission(String nomPermission) {
        return new NomPermissionDejaExistantException("Le nom permission existe deja : " + nomPermission);
    }

}

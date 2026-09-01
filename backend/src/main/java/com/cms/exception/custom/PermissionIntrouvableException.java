package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'une permission n'existe pas.
 */
public class PermissionIntrouvableException extends CmsException {

    public PermissionIntrouvableException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public static PermissionIntrouvableException pourId(Long id) {
        return new PermissionIntrouvableException("Permission introuvable avec l'identifiant : " + id);
    }

    public static PermissionIntrouvableException pourNomPermission(String nomPermission) {
        return new PermissionIntrouvableException("Permission introuvable avec le nom : " + nomPermission);
    }

}

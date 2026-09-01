package com.cms.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * Exception levee lorsqu'une permission est deja attribuee a un profil ou a un
 * utilisateur (unicite des associations RBAC).
 */
public class PermissionDejaAttribueeException extends CmsException {

    public PermissionDejaAttribueeException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }

    public static PermissionDejaAttribueeException pourProfil(Long profilId, Long permissionId) {
        return new PermissionDejaAttribueeException(
                "La permission " + permissionId + " est deja attribuee au profil " + profilId);
    }

    public static PermissionDejaAttribueeException pourUtilisateur(Long utilisateurId, Long permissionId, String type) {
        return new PermissionDejaAttribueeException(
                "La permission " + permissionId + " est deja en " + type + " pour l'utilisateur " + utilisateurId);
    }

}

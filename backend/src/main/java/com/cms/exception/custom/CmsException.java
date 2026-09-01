package com.cms.exception.custom;

/**
 * Exception de base de l'application.
 * Toutes les exceptions metier heriteront de cette classe.
 */
public abstract class CmsException extends RuntimeException {

    private final int httpStatus;

    protected CmsException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}

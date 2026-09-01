package com.cms.exception.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cms.common.constants.Messages;
import com.cms.common.response.ErrorResponse;
import com.cms.exception.custom.CmsException;

import jakarta.validation.ConstraintViolationException;

/**
 * Gestion centralisee des exceptions.
 *
 * <p>Convertit toutes les erreurs en {@link ErrorResponse} avec un format
 * uniforme ({@code success}, {@code message}, {@code code}, {@code timestamp},
 * {@code errors}). Les erreurs metier utilisent le statut HTTP porte par
 * {@link CmsException}. Les erreurs de validation DTO sont detaillees champ
 * par champ. Les erreurs inattendues ne retournent jamais d'exception Spring
 * brute au frontend et sont loggees sans information sensible (mot de passe,
 * token).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    /**
     * Erreur metier : statut HTTP porte par l'exception.
     */
    @ExceptionHandler(CmsException.class)
    public ResponseEntity<ErrorResponse> handleCmsException(CmsException ex) {
        String code = codeDe(ex);
        int status = ex.getHttpStatus();
        if (status >= 500) {
            log.error("Erreur serveur ({} - HTTP {}) : {}", code, status, ex.getMessage());
        } else {
            log.warn("Erreur metier ({} - HTTP {}) : {}", code, status, ex.getMessage());
        }
        return ResponseEntity.status(status).body(ErrorResponse.of(code, ex.getMessage()));
    }

    /**
     * Validation de DTO (@RequestBody) : erreurs detaillees par champ.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationDto(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (premier, suivant) -> premier, LinkedHashMap::new));
        log.warn("Validation de DTO echouee : {}", erreurs);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(VALIDATION_ERROR, "Erreur de validation", erreurs));
    }

    /**
     * Validation de parametres / chemins (@RequestParam, @PathVariable...).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationParametres(ConstraintViolationException ex) {
        Map<String, String> erreurs = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        jakarta.validation.ConstraintViolation::getMessage,
                        (premier, suivant) -> premier, LinkedHashMap::new));
        log.warn("Validation de parametres echouee : {}", erreurs);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(VALIDATION_ERROR, "Erreur de validation", erreurs));
    }

    /**
     * Corps de requete illisible (JSON malforme).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleRequeteIllisible(HttpMessageNotReadableException ex) {
        log.warn("Corps de requete illisible : {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("REQUETE_INVALIDE", "Corps de requete invalide"));
    }

    /**
     * Droits insuffisants sur un endpoint protege (@PreAuthorize).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acces refuse : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCES_REFUSE", Messages.ACCES_REFUSE));
    }

    /**
     * Echec d'authentification (identifiants invalides).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentification echouee : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("AUTHENTIFICATION_REQUISE", Messages.AUTHENTIFICATION_REQUISE));
    }

    /**
     * Erreur inattendue : jamais renvoyee telle quelle au frontend.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptionInattendue(Exception ex) {
        log.error("Exception inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("ERREUR_INTERNE", Messages.ERREUR_GENERIQUE));
    }

    /**
     * Derive un code machine lisible du nom de l'exception.
     * Ex : EmailDejaUtiliseException -> EMAIL_DEJA_UTILISE.
     */
    private String codeDe(CmsException ex) {
        String nom = ex.getClass().getSimpleName();
        if (nom.endsWith("Exception")) {
            nom = nom.substring(0, nom.length() - "Exception".length());
        }
        return nom.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

}

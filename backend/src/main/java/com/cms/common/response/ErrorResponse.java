package com.cms.common.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Format unique des reponses d'erreur retournees au frontend.
 *
 * <pre>
 * {
 *   "success": false,
 *   "message": "Email deja utilise",
 *   "code": "EMAIL_DEJA_UTILISE",
 *   "timestamp": "2026-08-06T00:00:00",
 *   "errors": { "email": "Format invalide" }
 * }
 * </pre>
 *
 * <p>{@code errors} est absent (ou {@code null}) hors erreur de validation ;
 * present sous forme de map {@code champ -> message} en cas d'erreur de
 * validation de DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    /**
     * Erreur simple (sans detail par champ).
     *
     * @param code    code machine (ex : EMAIL_DEJA_UTILISE)
     * @param message message lisible
     * @return reponse d'erreur
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, message, code, LocalDateTime.now(), null);
    }

    /**
     * Erreur de validation avec le detail des champs.
     *
     * @param code    code machine (ex : VALIDATION_ERROR)
     * @param message message lisible
     * @param errors  map {@code champ -> message}
     * @return reponse d'erreur
     */
    public static ErrorResponse of(String code, String message, Map<String, String> errors) {
        return new ErrorResponse(false, message, code, LocalDateTime.now(), errors);
    }

}

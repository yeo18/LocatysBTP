package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.ValidationTacheStatut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une validation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTacheResumeResponse {

    private Long id;
    private Long tacheId;
    private ValidationTacheStatut statut;
    private LocalDate dateValidation;
}

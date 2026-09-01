package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.ValidationTacheStatut;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une validation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateValidationTacheRequest {

    @NotNull
    private Long tacheId;

    @NotNull
    private ValidationTacheStatut statut;

    @Size(max = 500)
    private String commentaire;

    @NotNull
    private LocalDate dateValidation;
}

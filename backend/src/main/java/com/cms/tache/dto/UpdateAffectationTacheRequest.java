package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.AffectationTacheRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'une affectation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAffectationTacheRequest {

    private Long utilisateurId;

    private Long equipeId;

    @NotNull
    private AffectationTacheRole role;

    @NotNull
    private LocalDate dateAffectation;
}

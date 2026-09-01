package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.AffectationTacheRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une affectation de tache.
 *
 * <p>Regle metier : au moins une cible (utilisateur OU equipe) â€” controlee
 * dans le Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAffectationTacheRequest {

    @NotNull
    private Long tacheId;

    private Long utilisateurId;

    private Long equipeId;

    @NotNull
    private AffectationTacheRole role;

    @NotNull
    private LocalDate dateAffectation;
}

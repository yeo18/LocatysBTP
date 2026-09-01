package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une affectation equipe - chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAffectationEquipeChantierRequest {

    @NotNull
    private Long equipeId;

    @NotNull
    private Long chantierId;

    @NotNull
    private LocalDate dateDebut;

    private LocalDate dateFin;

    private AffectationEquipeChantierStatut statut = AffectationEquipeChantierStatut.ACTIVE;
}

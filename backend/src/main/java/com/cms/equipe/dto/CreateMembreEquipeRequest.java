package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.RoleDansEquipe;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'un membre d'equipe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMembreEquipeRequest {

    @NotNull
    private Long utilisateurId;

    @NotNull
    private Long equipeId;

    @NotNull
    private RoleDansEquipe roleDansEquipe;

    @NotNull
    private LocalDate dateIntegration;
}

package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.RoleDansEquipe;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'un membre d'equipe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMembreEquipeRequest {

    @NotNull
    private RoleDansEquipe roleDansEquipe;

    @NotNull
    private LocalDate dateIntegration;
}

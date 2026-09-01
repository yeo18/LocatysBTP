package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.RoleDansEquipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un membre d'equipe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembreEquipeResumeResponse {

    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private Long equipeId;
    private String equipeNom;
    private RoleDansEquipe roleDansEquipe;
}

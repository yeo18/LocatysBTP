package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une affectation equipe - chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationEquipeChantierResumeResponse {

    private Long id;
    private String equipeNom;
    private String chantierNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private AffectationEquipeChantierStatut statut;
}

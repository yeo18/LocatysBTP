package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une affectation equipe - chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationEquipeChantierResponse {

    private Long id;
    private Long equipeId;
    private String equipeNom;
    private Long chantierId;
    private String chantierNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private AffectationEquipeChantierStatut statut;
}

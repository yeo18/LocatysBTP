package com.cms.equipe.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une equipe pour la liste : metadonnees +
 * membres + affectation chantier active.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipeDetailResponse {

    private Long id;
    private String nom;
    private String description;
    private List<MembreEquipeResponse> membres;
    private List<AffectationEquipeChantierResponse> affectations;
}
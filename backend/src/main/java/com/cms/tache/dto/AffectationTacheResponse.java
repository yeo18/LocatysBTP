package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.AffectationTacheRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une affectation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationTacheResponse {

    private Long id;
    private Long tacheId;
    private String tacheTitre;
    private Long utilisateurId;
    private String utilisateurNom;
    private Long equipeId;
    private String equipeNom;
    private AffectationTacheRole role;
    private LocalDate dateAffectation;
}

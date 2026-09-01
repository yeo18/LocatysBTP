package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.AffectationTacheRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une affectation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationTacheResumeResponse {

    private Long id;
    private Long tacheId;
    private Long utilisateurId;
    private Long equipeId;
    private AffectationTacheRole role;
    private LocalDate dateAffectation;
}

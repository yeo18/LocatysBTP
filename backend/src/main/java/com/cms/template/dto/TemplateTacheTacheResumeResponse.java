package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RÃ©ponse rÃ©sumÃ©e d'une tÃ¢che structurÃ©e d'un {@code TemplateTache}
 * (utilisÃ©e notamment pour la liste ordonnÃ©e dans le dÃ©tail).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTacheTacheResumeResponse {

    private Long id;
    private String titre;
    private Priorite priorite;
    private Integer dureeEstimeeJours;
}
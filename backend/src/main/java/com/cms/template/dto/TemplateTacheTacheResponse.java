package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RÃ©ponse dÃ©taillÃ©e d'une tÃ¢che structurÃ©e d'un {@code TemplateTache}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTacheTacheResponse {

    private Long id;
    private Long templateTacheId;
    private String titre;
    private String description;
    private Priorite priorite;
    private Integer dureeEstimeeJours;
}
package com.cms.template.dto;


import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.enums.TypeConstruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un template de chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateChantierResponse {

    private Long id;
    private String nom;
    private String description;
    private TypeConstruction typeConstruction;
    private Integer dureeEstimeeJours;
    private TemplateChantierStatut statut;
}

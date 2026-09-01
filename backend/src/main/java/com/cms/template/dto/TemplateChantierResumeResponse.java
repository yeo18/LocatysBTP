package com.cms.template.dto;

import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.enums.TypeConstruction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un template de chantier (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateChantierResumeResponse {

    private Long id;
    private String nom;
    private TypeConstruction typeConstruction;
    private TemplateChantierStatut statut;
}

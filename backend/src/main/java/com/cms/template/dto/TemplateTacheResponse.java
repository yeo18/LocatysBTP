package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un template de tache (groupe reutilisable).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTacheResponse {

    private Long id;
    private String titre;
    private String description;
    private Priorite priorite;
    private Integer dureeEstimeeJours;
}

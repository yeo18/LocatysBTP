package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un template de tache (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTacheResumeResponse {

    private Long id;
    private String titre;
    private Priorite priorite;
}

package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de modification d'un template de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateTacheRequest {

    @NotBlank
    @Size(max = 255)
    private String titre;

    @Size(max = 1000)
    private String description;

    private Priorite priorite = Priorite.MOYENNE;

    @Positive
    private Integer dureeEstimeeJours;
}

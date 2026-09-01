package com.cms.template.dto;


import com.cms.template.entity.enums.TypeConstruction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de modification d'un template de chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateChantierRequest {

    @NotBlank
    @Size(max = 255)
    private String nom;

    @Size(max = 1000)
    private String description;

    @NotNull
    private TypeConstruction typeConstruction;

    @Positive
    private Integer dureeEstimeeJours;
}

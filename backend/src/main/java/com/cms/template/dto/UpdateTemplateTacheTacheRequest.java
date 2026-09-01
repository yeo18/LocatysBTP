package com.cms.template.dto;

import com.cms.tache.entity.enums.Priorite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RequÃªte de modification d'une tÃ¢che structurÃ©e d'un {@code TemplateTache}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTemplateTacheTacheRequest {

    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 255, message = "Le titre ne doit pas dÃ©passer 255 caractÃ¨res.")
    private String titre;

    @Size(max = 1000, message = "La description ne doit pas dÃ©passer 1000 caractÃ¨res.")
    private String description;

    @Schema(description = "PrioritÃ© de la tÃ¢che (dÃ©faut MOYENNE)")
    private Priorite priorite;

    @Positive(message = "La durÃ©e estimÃ©e doit Ãªtre strictement positive.")
    private Integer dureeEstimeeJours;
}
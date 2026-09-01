package com.cms.chantier.dto;

import java.time.LocalDate;

import com.cms.chantier.entity.enums.ChantierStatut;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'un chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChantierRequest {

    @NotBlank
    @Size(max = 255)
    private String nom;

    @Size(max = 1000)
    private String description;

    @Size(max = 255)
    private String adresseSaisie;

    private ChantierStatut statut = ChantierStatut.PREVU;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Long responsableId;
}

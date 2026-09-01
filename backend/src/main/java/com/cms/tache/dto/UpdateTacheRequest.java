package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.enums.TacheStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'une tache.
 *
 * <p>Version stricte (LOOP 3.15) : le changement de statut et de progression
 * passe par cette requete ; l'affectation et la validation sont gerees par
 * leurs services dedies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTacheRequest {

    @NotBlank
    @Size(max = 255)
    private String titre;

    @Size(max = 1000)
    private String description;

    private Priorite priorite = Priorite.MOYENNE;

    private TacheStatus status = TacheStatus.A_FAIRE;

    @Min(0)
    @Max(100)
    private Integer progression = 0;

    private LocalDate dateDebut;

    private LocalDate dateFin;
}

package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.Priorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une tache.
 *
 * <p>Version stricte (LOOP 3.15) : seuls les champs valides sont exposes.
 * Le statut est initialise a A_FAIRE, la progression a 0 dans le service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTacheRequest {

    @NotBlank
    @Size(max = 255)
    private String titre;

    @Size(max = 1000)
    private String description;

    private Priorite priorite = Priorite.MOYENNE;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @NotNull
    private Long chantierId;
}

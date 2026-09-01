package com.cms.chantier.dto;

import java.time.LocalDate;

import com.cms.chantier.entity.enums.ChantierStatut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un chantier (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChantierResumeResponse {

    private Long id;
    private String nom;
    private String adresseSaisie;
    private ChantierStatut statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int progression;
}

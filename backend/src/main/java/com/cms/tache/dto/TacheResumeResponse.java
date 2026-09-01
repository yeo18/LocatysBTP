package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.enums.TacheStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une tache (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TacheResumeResponse {

    private Long id;
    private String titre;
    private Priorite priorite;
    private TacheStatus status;
    private int progression;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long chantierId;
}

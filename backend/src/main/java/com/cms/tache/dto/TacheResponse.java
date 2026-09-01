package com.cms.tache.dto;

import java.time.LocalDate;

import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.enums.TacheStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une tache (version stricte LOOP 3.15).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TacheResponse {

    private Long id;
    private String titre;
    private String description;
    private Priorite priorite;
    private TacheStatus status;
    private int progression;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long chantierId;
    private String chantierNom;
}

package com.cms.tache.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cms.tache.entity.enums.ValidationTacheStatut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une validation de tache.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTacheResponse {

    private Long id;
    private Long tacheId;
    private String tacheTitre;
    private Long validateurId;
    private String validateurNom;
    private String validateurPrenom;
    private ValidationTacheStatut statut;
    private String commentaire;
    private LocalDate dateValidation;
    private LocalDateTime dateModification;
}

package com.cms.chantier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.entity.enums.FiabiliteCoordonnees;
import com.cms.chantier.entity.enums.OrigineCoordonnees;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChantierResponse {

    private Long id;
    private String nom;
    private String description;
    private String adresseSaisie;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String adresseGeocodee;
    private OrigineCoordonnees origineCoordonnees;
    private FiabiliteCoordonnees fiabiliteCoordonnees;
    private ChantierStatut statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int progression;
    private Long responsableId;
    private String responsableNom;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

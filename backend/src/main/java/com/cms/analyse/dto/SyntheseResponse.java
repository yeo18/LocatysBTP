package com.cms.analyse.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Synthèse de l'analyse de site.
 *
 * <p>Produite uniquement à partir des données réellement récupérées
 * (météo, environnement). Aucune information inventée.
 * C'est une aide à la préparation du chantier, elle ne remplace pas une
 * expertise technique, géotechnique ou structurelle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyntheseResponse {

    private String niveauVigilance;
    private List<String> pointsFavorables;
    private List<String> pointsAttention;
    private List<String> recommandations;
    private String resume;

}

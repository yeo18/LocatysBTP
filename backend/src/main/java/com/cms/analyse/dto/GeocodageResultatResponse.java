package com.cms.analyse.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Résultat d'une recherche de géocodage Nominatim.
 *
 * <p>Position proposée (non confirmée) : l'utilisateur doit la vérifier sur
 * la carte avant de la confirmer pour le chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeocodageResultatResponse {

    private String libelle;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String type;
    private String ville;
    private String commune;
    private String quartier;
    private String pays;

}

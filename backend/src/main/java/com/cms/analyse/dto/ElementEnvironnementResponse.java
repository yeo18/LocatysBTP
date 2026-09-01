package com.cms.analyse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Élément d'environnement trouvé par Overpass autour du chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElementEnvironnementResponse {

    private String nom;
    private String type;
    private double latitude;
    private double longitude;
    private double distanceKm;

}

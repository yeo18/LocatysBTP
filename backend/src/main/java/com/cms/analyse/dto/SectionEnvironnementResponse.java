package com.cms.analyse.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Section environnement (points d'intérêt OSM via Overpass) d'une analyse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionEnvironnementResponse {

    private StatutSource statut;
    private String message;
    private int rayonMetres;
    private List<ElementEnvironnementResponse> elements;

}

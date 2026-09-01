package com.cms.analyse.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Section météo d'une analyse de site.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionMeteoResponse {

    private StatutSource statut;
    private String message;
    private MeteoActuelleResponse actuel;
    private List<MeteoQuotidienneResponse> quotidiennes;
    private List<MeteoHoraireResponse> horaires;
    /** Qualité de l'air au point d'analyse (null si indisponible). */
    private QualiteAirResponse qualiteAir;

}

package com.cms.analyse.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Résultat complet de l'analyse d'un site.
 *
 * <p>Chaque section est indépendante : le statut d'une source n'affecte pas
 * les autres (une source indisponible ne fait jamais échouer l'analyse).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyseSiteResponse {

    private Long chantierId;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private SectionMeteoResponse meteo;
    private SectionEnvironnementResponse environnement;
    private SyntheseResponse synthese;

}
package com.cms.analyse.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données météo actuelles (source OpenWeather).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeteoActuelleResponse {

    /** Température (°C). */
    private double temperature;
    /** Température ressentie (°C). */
    private double temperatureRessentie;
    /** Humidité relative (%). */
    private double humidite;
    /** Pression atmosphérique (hPa). */
    private double pression;
    /** Précipitations pluie observées (mm). */
    private double precipitations;
    /** Précipitations neige observées (mm équivalent eau). */
    private double neige;
    /** Vitesse du vent (km/h). */
    private double vent;
    /** Direction du vent (degrés de boussole, 0-360). */
    private double directionVent;
    /** Rafales de vent (km/h). */
    private double rafales;
    /** Couverture nuageuse (%). */
    private double couvertureNuageuse;
    /** Visibilité (mètres). */
    private double visibilite;
    /** Condition principale OpenWeather (ex : Clear, Rain). */
    private String conditions;
    /** Description lisible (ex : « ciel dégagé », langue configurée). */
    private String description;
    /** Code condition OpenWeather (ex : 800 = ciel dégagé). */
    private int codeMeteo;
    /** Heure de mise à jour des données. */
    private LocalDateTime heureMiseAJour;

}
package com.cms.analyse.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prévision météo horaire (pas de 3 h, 5 jours, source OpenWeather).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeteoHoraireResponse {

    private LocalDateTime dateHeure;
    /** Température (°C). */
    private double temperature;
    /** Température ressentie (°C). */
    private double temperatureRessentie;
    /** Précipitations sur le pas (mm). */
    private double precipitations;
    /** Probabilité de précipitations (%). */
    private double probaPrecipitations;
    /** Vitesse du vent (km/h). */
    private double vent;
    /** Humidité relative (%). */
    private double humidite;
    /** Couverture nuageuse (%). */
    private double couvertureNuageuse;
    /** Description lisible de la condition. */
    private String description;
    /** Code condition OpenWeather. */
    private int codeMeteo;

}
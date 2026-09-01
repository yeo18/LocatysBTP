package com.cms.analyse.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prévision météo quotidienne (jusqu'à 5 jours, limite du plan gratuit
 * OpenWeather, agrégée depuis les prévisions à pas de 3 h).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeteoQuotidienneResponse {

    private LocalDate date;
    /** Température maximale (°C). */
    private double tempMax;
    /** Température minimale (°C). */
    private double tempMin;
    /** Cumul de précipitations du jour (mm). */
    private double precipitations;
    /** Probabilité maximale de précipitations du jour (%). */
    private double probaPrecipitations;
    /** Vent maximal (km/h). */
    private double ventMax;
    /** Rafales maximales (km/h). */
    private double rafales;
    /** Humidité maximale de la journée (%). */
    private double humiditeMax;
    /** Couverture nuageuse moyenne de la journée (%). */
    private double couvertureNuageuse;
    /** Description lisible de la condition dominante du jour. */
    private String description;
    /** Code condition OpenWeather. */
    private int codeMeteo;

}
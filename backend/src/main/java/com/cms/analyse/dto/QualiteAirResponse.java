package com.cms.analyse.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Qualité de l'air au point d'analyse (source OpenWeather « Air Pollution »,
 * plan gratuit, valeurs en µg/m³).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualiteAirResponse {

    /** Indice qualité de l'air (1 = Bon, 2 = Correct, 3 = Moyen, 4 = Mauvais, 5 = Très mauvais). */
    private int aqi;
    /** Libellé lisible de l'indice (« Bon », « Correct », ...). */
    private String libelle;
    /** Particules fines PM2.5 (µg/m³). */
    private double pm25;
    /** Particules PM10 (µg/m³). */
    private double pm10;
    /** Ozone O3 (µg/m³). */
    private double o3;
    /** Dioxyde d'azote NO2 (µg/m³). */
    private double no2;
    /** Dioxyde de soufre SO2 (µg/m³). */
    private double so2;
    /** Monoxyde de carbone CO (µg/m³). */
    private double co;
    /** Heure de mise à jour des mesures. */
    private LocalDateTime heureMiseAJour;

}
package com.cms.analyse.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.MeteoQuotidienneResponse;
import com.cms.analyse.dto.SectionMeteoResponse;
import com.cms.analyse.dto.SyntheseResponse;

/**
 * Génère la synthèse d'une analyse de site à partir des données
 * réellement récupérées (météo, environnement).
 *
 * <p>Synthèse déterministe par règles métier : aucun modèle d'IA n'est
 * intégré au backend (aucune infrastructure d'IA dans le projet). Elle ne
 * fabrique aucune information : seules les données reçues alimentent les
 * conclusions. C'est une aide à la préparation du chantier, elle ne remplace
 * pas une expertise technique, géotechnique ou structurelle.
 */
@Service
public class SyntheseService {

    /**
     * Produit la synthèse d'une analyse complète.
     *
     * @param analyse résultats des sources (dont au moins une peut être
     *                indisponible)
     * @return synthèse structurée
     */
    public SyntheseResponse synthetiser(AnalyseSiteResponse analyse) {
        List<String> favorables = new ArrayList<>();
        List<String> attention = new ArrayList<>();
        List<String> recommandations = new ArrayList<>();

        analyserMeteo(analyse.getMeteo(), favorables, attention, recommandations);
        analyserQualiteAir(analyse.getMeteo(), favorables, attention, recommandations);

        String niveau = determinerNiveau(attention);
        String resume = construireResume(analyse, attention, favorables);

        return new SyntheseResponse(niveau, favorables, attention, recommandations, resume);
    }

    private void analyserMeteo(SectionMeteoResponse meteo,
                               List<String> favorables,
                               List<String> attention,
                               List<String> recommandations) {
        if (meteo == null || meteo.getActuel() == null || meteo.getQuotidiennes() == null) {
            return;
        }

        double pluieTotale = meteo.getQuotidiennes().stream()
                .mapToDouble(MeteoQuotidienneResponse::getPrecipitations).sum();
        double probaMax = meteo.getQuotidiennes().stream()
                .mapToDouble(MeteoQuotidienneResponse::getProbaPrecipitations).max().orElse(0);

        if (pluieTotale > 30) {
            attention.add("Précipitations importantes prévues sur la période ("
                    + Math.round(pluieTotale) + " mm cumulés sur "
                    + meteo.getQuotidiennes().size() + " jours).");
            recommandations.add("Prévoir l'évacuation des eaux et protéger les zones de travail ouvertes.");
        } else if (pluieTotale > 0) {
            favorables.add("Pluviométrie modérée sur la période (" + Math.round(pluieTotale) + " mm).");
        } else {
            favorables.add("Aucune précipitation significative prévue sur la période.");
        }

        if (probaMax > 70) {
            attention.add("Probabilité de précipitations élevée (" + Math.round(probaMax) + " %) certains jours.");
            recommandations.add("Vérifier l'accessibilité du chantier en cas de pluie.");
        }

        double tempMax = meteo.getQuotidiennes().stream()
                .mapToDouble(MeteoQuotidienneResponse::getTempMax).max().orElse(0);
        if (tempMax > 35) {
            attention.add("Températures maximales élevées (jusqu'à " + Math.round(tempMax) + " °C).");
            recommandations.add("Organiser le travail tôt le matin et prévoir de l'hydratation.");
        }

        double ventMax = meteo.getQuotidiennes().stream()
                .mapToDouble(MeteoQuotidienneResponse::getVentMax).max().orElse(0);
        if (ventMax > 50) {
            attention.add("Vents forts possibles (jusqu'à " + Math.round(ventMax) + " km/h).");
            recommandations.add("Sécuriser les échafaudages et les matériaux légers.");
        }

        double humiditeMax = meteo.getQuotidiennes().stream()
                .mapToDouble(MeteoQuotidienneResponse::getHumiditeMax).max().orElse(0);
        if (humiditeMax > 90) {
            attention.add("Taux d'humidité élevé sur la période (jusqu'à " + Math.round(humiditeMax) + " %).");
            recommandations.add("Limiter les travaux sensibles à l'humidité (peinture, scellement, coffrage) aux heures sèches.");
        }
    }

    private void analyserQualiteAir(SectionMeteoResponse meteo,
                                    List<String> favorables,
                                    List<String> attention,
                                    List<String> recommandations) {
        if (meteo == null || meteo.getQualiteAir() == null || meteo.getQualiteAir().getAqi() <= 0) {
            return;
        }
        var qualite = meteo.getQualiteAir();
        int aqi = qualite.getAqi();
        if (aqi >= 4) {
            attention.add("Indice de qualité de l'air mauvais (" + qualite.getLibelle() + ", " + aqi + "/5).");
            recommandations.add("Limiter les activités poussiéreuses et porter un masque lors des travaux extérieurs.");
        } else if (aqi == 3) {
            attention.add("Indice de qualité de l'air moyen (" + qualite.getLibelle() + ", " + aqi + "/5).");
            recommandations.add("Surveiller l'indice de qualité de l'air avant les travaux extérieurs.");
        } else {
            favorables.add("Indice de qualité de l'air correct (" + qualite.getLibelle() + ", " + aqi + "/5).");
        }

        if (qualite.getPm25() > 25 || qualite.getPm10() > 50) {
            attention.add("Particules fines élevées (PM2.5 " + Math.round(qualite.getPm25())
                    + ", PM10 " + Math.round(qualite.getPm10()) + " µg/m³).");
            recommandations.add("Réduire les émissions de poussière et protéger les équipements sensibles.");
        }
    }

    private String determinerNiveau(List<String> attention) {
        if (attention.size() >= 4) {
            return "ELEVEE";
        }
        if (attention.size() >= 2) {
            return "MODEREE";
        }
        if (attention.size() == 1) {
            return "LEGERE";
        }
        return "AUCUNE";
    }

    private String construireResume(AnalyseSiteResponse analyse,
                                    List<String> attention,
                                    List<String> favorables) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse du site réalisée à partir des données publiques disponibles. ");
        if (attention.isEmpty()) {
            sb.append("Aucun point d'attention majeur identifié dans les sources consultées. ");
        } else {
            sb.append(attention.size()).append(" point(s) d'attention identifié(s). ");
        }
        if (favorables.isEmpty()) {
            sb.append("Les conditions locales restent à vérifier sur le terrain.");
        } else {
            sb.append("Points favorables : ").append(String.join(" ; ", favorables)).append('.');
        }
        sb.append(" Cette synthèse ne remplace pas une expertise technique, géotechnique ou structurelle.");
        return sb.toString();
    }
}
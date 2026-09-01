package com.cms.analyse.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.MeteoActuelleResponse;
import com.cms.analyse.dto.MeteoQuotidienneResponse;
import com.cms.analyse.dto.QualiteAirResponse;
import com.cms.analyse.dto.SectionMeteoResponse;
import com.cms.analyse.dto.StatutSource;
import com.cms.analyse.dto.SyntheseResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la synthèse déterministe (règles météo + qualité de
 * l'air).
 */
class SyntheseServiceTest {

    private final SyntheseService service = new SyntheseService();

    private SyntheseResponse synthetiser(SectionMeteoResponse meteo) {
        AnalyseSiteResponse analyse = new AnalyseSiteResponse();
        analyse.setMeteo(meteo);
        return service.synthetiser(analyse);
    }

    private QualiteAirResponse qualiteAir(int aqi, double pm25, double pm10) {
        QualiteAirResponse q = new QualiteAirResponse();
        q.setAqi(aqi);
        q.setLibelle(aqi == 1 ? "Bon" : aqi == 2 ? "Correct" : aqi == 3 ? "Moyen"
                : aqi == 4 ? "Mauvais" : "Très mauvais");
        q.setPm25(pm25);
        q.setPm10(pm10);
        return q;
    }

    private SectionMeteoResponse meteo(double pluieTotaleMm, int jours, QualiteAirResponse air) {
        MeteoActuelleResponse actuel = new MeteoActuelleResponse();
        var quotidiennes = new java.util.ArrayList<MeteoQuotidienneResponse>();
        for (int i = 0; i < jours; i++) {
            MeteoQuotidienneResponse q = new MeteoQuotidienneResponse();
            q.setDate(LocalDate.of(2026, 8, 20).plusDays(i));
            q.setTempMax(28);
            q.setTempMin(22);
            q.setPrecipitations(jours == 0 ? 0 : pluieTotaleMm / jours);
            q.setProbaPrecipitations(50);
            q.setVentMax(15);
            q.setRafales(25);
            q.setDescription("ciel dégagé");
            q.setCodeMeteo(800);
            quotidiennes.add(q);
        }
        return new SectionMeteoResponse(StatutSource.DISPONIBLE, null, actuel,
                quotidiennes, List.of(), air);
    }

    @Test
    void qualiteAir_mauvaise_ajouteAttentionEtRecommandation() {
        SyntheseResponse resultat = synthetiser(meteo(0, 1, qualiteAir(4, 20, 30)));

        assertThat(resultat.getPointsAttention()).anyMatch(a -> a.contains("Indice de qualité de l'air mauvais (Mauvais, 4/5)"));
        assertThat(resultat.getRecommandations()).anyMatch(a -> a.contains("masque"));
        assertThat(resultat.getNiveauVigilance()).isEqualTo("LEGERE");
    }

    @Test
    void qualiteAir_bonne_ajoutePointFavorable() {
        SyntheseResponse resultat = synthetiser(meteo(0, 1, qualiteAir(1, 5, 10)));

        assertThat(resultat.getPointsFavorables()).anyMatch(a -> a.contains("Indice de qualité de l'air correct (Bon, 1/5)"));
    }

    @Test
    void qualiteAir_particulesFinesElevees_ajouteAttention() {
        SyntheseResponse resultat = synthetiser(meteo(0, 1, qualiteAir(2, 40, 30)));

        assertThat(resultat.getPointsAttention()).anyMatch(a -> a.contains("Particules fines élevées (PM2.5 40"));
    }

    @Test
    void qualiteAir_null_aucuneMentionAir() {
        SyntheseResponse resultat = synthetiser(meteo(0, 1, null));

        assertThat(resultat.getPointsAttention()).allMatch(a -> !a.startsWith("Indice de qualité de l'air"));
        assertThat(resultat.getPointsAttention()).noneMatch(a -> a.contains("Particules fines"));
    }

    @Test
    void pluieImportante_messageUtiliseLeNombreDeJoursReels() {
        SyntheseResponse resultat = synthetiser(meteo(40, 5, null));

        assertThat(resultat.getPointsAttention()).anyMatch(a -> a.contains("mm cumulés sur 5 jours"));
        assertThat(resultat.getPointsAttention()).noneMatch(a -> a.contains("16 jours"));
    }

    @Test
    void humiditeElevee_ajouteAttentionEtRecommandation() {
        var quotidiennes = new java.util.ArrayList<MeteoQuotidienneResponse>();
        MeteoQuotidienneResponse q = new MeteoQuotidienneResponse();
        q.setDate(LocalDate.of(2026, 8, 20));
        q.setTempMax(28);
        q.setTempMin(22);
        q.setPrecipitations(0);
        q.setProbaPrecipitations(50);
        q.setVentMax(15);
        q.setRafales(25);
        q.setHumiditeMax(95);
        q.setDescription("ciel dégagé");
        q.setCodeMeteo(800);
        quotidiennes.add(q);

        SectionMeteoResponse meteo = new SectionMeteoResponse(StatutSource.DISPONIBLE, null,
                new MeteoActuelleResponse(), quotidiennes, List.of(), null);

        SyntheseResponse resultat = synthetiser(meteo);

        assertThat(resultat.getPointsAttention()).anyMatch(a -> a.contains("Taux d'humidité élevé sur la période (jusqu'à 95 %)."));
        assertThat(resultat.getRecommandations()).anyMatch(a -> a.contains("peinture, scellement, coffrage"));
    }

    @Test
    void humiditeForte_absente_pasDeMentionHumidite() {
        SyntheseResponse resultat = synthetiser(meteo(0, 1, null));

        assertThat(resultat.getPointsAttention()).noneMatch(a -> a.contains("Taux d'humidité"));
    }

}
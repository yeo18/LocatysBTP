package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;

import com.cms.equipe.dto.AffectationEquipeChantierResponse;
import com.cms.equipe.dto.AffectationEquipeChantierResumeResponse;

/**
 * Service metier des affectations equipe - chantier.
 *
 * <p>Une equipe peut travailler sur plusieurs chantiers ; un chantier peut
 * avoir plusieurs equipes. Cette relation porte la securite des donnees
 * (visibilite des chantiers par equipe).
 */
public interface AffectationEquipeChantierService {

    /**
     * Affecte une equipe a un chantier pour une periode.
     *
     * @param equipeId   identifiant de l'equipe
     * @param chantierId identifiant du chantier
     * @param dateDebut  debut de l'affectation (obligatoire)
     * @param dateFin    fin de l'affectation (optionnelle, >= dateDebut)
     * @return affectation creee
     */
    AffectationEquipeChantierResponse affecter(Long equipeId, Long chantierId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Termine une affectation (statut = TERMINEE).
     *
     * @param affectationId identifiant de l'affectation
     * @return affectation terminee
     */
    AffectationEquipeChantierResponse terminer(Long affectationId);

    /**
     * Liste les equipes affectees a un chantier.
     *
     * @param chantierId identifiant du chantier
     * @return affectations du chantier
     */
    List<AffectationEquipeChantierResumeResponse> listerEquipesDuChantier(Long chantierId);

    /**
     * Liste les chantiers d'une equipe.
     *
     * @param equipeId identifiant de l'equipe
     * @return affectations de l'equipe
     */
    List<AffectationEquipeChantierResponse> listerChantiersDeEquipe(Long equipeId);

}

package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;

import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.dto.MembreEquipeResumeResponse;
import com.cms.equipe.entity.enums.RoleDansEquipe;

/**
 * Service metier des appartenances utilisateur - equipe.
 *
 * <p>Un utilisateur peut appartenir a plusieurs equipes ; une equipe peut
 * avoir plusieurs membres. Le role d'un membre est uniquement
 * {@code CHEF} ou {@code OUVRIER} (aucune entite ChefEquipe / ResponsableEquipe).
 */
public interface MembreEquipeService {

    /**
     * Integre un utilisateur dans une equipe.
     *
     * @param equipeId       identifiant de l'equipe
     * @param utilisateurId  identifiant de l'utilisateur
     * @param role           role dans l'equipe (CHEF / OUVRIER)
     * @param dateIntegration date d'integration
     * @return membre cree
     */
    MembreEquipeResponse integrer(Long equipeId, Long utilisateurId, RoleDansEquipe role, LocalDate dateIntegration);

    /**
     * Change le role d'un membre (CHEF / OUVRIER).
     *
     * @param membreEquipeId identifiant de l'appartenance
     * @param role           nouveau role
     * @return membre modifie
     */
    MembreEquipeResponse changerRole(Long membreEquipeId, RoleDansEquipe role);

    /**
     * Retire un utilisateur d'une equipe.
     *
     * @param membreEquipeId identifiant de l'appartenance
     */
    void retirer(Long membreEquipeId);

    /**
     * Liste les membres d'une equipe.
     *
     * @param equipeId identifiant de l'equipe
     * @return membres de l'equipe
     */
    List<MembreEquipeResponse> listerMembres(Long equipeId);

    /**
     * Liste les equipes d'un utilisateur.
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @return equipes de l'utilisateur
     */
    List<MembreEquipeResumeResponse> listerEquipesDeUtilisateur(Long utilisateurId);

}

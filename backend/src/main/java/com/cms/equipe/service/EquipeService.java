package com.cms.equipe.service;

import java.util.List;

import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.EquipeResumeResponse;
import com.cms.equipe.dto.UpdateEquipeRequest;

/**
 * Service metier du module equipe.
 *
 * <p>Gestion du cycle de vie des equipes (creation, modification,
 * consultation). Le role de chef d'equipe est porte par
 * {@code MembreEquipe.roleDansEquipe} (CHEF/OUVRIER) : aucune entite
 * ChefEquipe ou ResponsableEquipe n'existe.
 */
public interface EquipeService {

    /**
     * Cree une equipe active par defaut.
     *
     * @param request donnees de creation (nom, description)
     * @return equipe creee
     */
    EquipeResponse creer(CreateEquipeRequest request);

    /**
     * Modifie les informations d'une equipe (nom, description).
     *
     * @param id      identifiant de l'equipe
     * @param request donnees de modification
     * @return equipe modifiee
     */
    EquipeResponse modifier(Long id, UpdateEquipeRequest request);

    /**
     * Consulte une equipe par identifiant.
     *
     * @param id identifiant de l'equipe
     * @return equipe
     */
    EquipeResponse trouverParId(Long id);

    /**
     * Liste toutes les equipes.
     *
     * @return equipes
     */
    List<EquipeResumeResponse> lister();

}

package com.cms.equipe.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.equipe.entity.MembreEquipe;

/**
 * Accès aux appartenances des utilisateurs à des équipes.
 */
public interface MembreEquipeRepository extends JpaRepository<MembreEquipe, Long> {

    List<MembreEquipe> findByUtilisateurId(Long utilisateurId);

    List<MembreEquipe> findByEquipeId(Long equipeId);

    List<MembreEquipe> findByEquipeIdIn(Collection<Long> equipeIds);
}

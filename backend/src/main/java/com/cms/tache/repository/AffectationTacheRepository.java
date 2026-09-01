package com.cms.tache.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.AffectationTache;

/**
 * Accès aux affectations de tâches.
 */
public interface AffectationTacheRepository extends JpaRepository<AffectationTache, Long> {

    List<AffectationTache> findByTacheId(Long tacheId);

    void deleteByTacheId(Long tacheId);

    List<AffectationTache> findByUtilisateurId(Long utilisateurId);

    List<AffectationTache> findByEquipeId(Long equipeId);
}

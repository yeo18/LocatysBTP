package com.cms.tache.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.AffectationTacheEquipe;

public interface AffectationTacheEquipeRepository extends JpaRepository<AffectationTacheEquipe, Long> {

    List<AffectationTacheEquipe> findByTacheId(Long tacheId);

    void deleteByTacheId(Long tacheId);

    List<AffectationTacheEquipe> findByEquipeId(Long equipeId);
}

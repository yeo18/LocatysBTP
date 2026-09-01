package com.cms.equipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.equipe.entity.Equipe;

/**
 * Accès aux données des équipes.
 */
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
}

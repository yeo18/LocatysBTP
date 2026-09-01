package com.cms.tache.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.ValidationTache;

/**
 * Accès aux validations de tâches.
 */
public interface ValidationTacheRepository extends JpaRepository<ValidationTache, Long> {

    List<ValidationTache> findByTacheId(Long tacheId);

    void deleteByTacheId(Long tacheId);
}

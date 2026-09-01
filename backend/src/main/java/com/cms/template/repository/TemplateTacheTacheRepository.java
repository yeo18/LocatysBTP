package com.cms.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.template.entity.TemplateTacheTache;

/**
 * Accès aux données des tâches structurées d'un {@code TemplateTache}.
 */
public interface TemplateTacheTacheRepository extends JpaRepository<TemplateTacheTache, Long> {

    List<TemplateTacheTache> findByTemplateTacheIdOrderByIdAsc(Long templateTacheId);

    void deleteByTemplateTacheId(Long templateTacheId);

    boolean existsByTemplateTacheIdAndTitreIgnoreCase(Long templateTacheId, String titre);

    Integer countByTemplateTacheId(Long templateTacheId);
}
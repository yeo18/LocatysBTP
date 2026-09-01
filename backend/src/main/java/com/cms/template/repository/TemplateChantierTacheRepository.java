package com.cms.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.template.entity.TemplateChantierTache;

/**
 * Accès aux associations TemplateChantier ↔ TemplateTache.
 */
public interface TemplateChantierTacheRepository extends JpaRepository<TemplateChantierTache, Long> {

    @EntityGraph(attributePaths = "templateTache")
    List<TemplateChantierTache> findByTemplateChantierIdOrderByIdAsc(Long templateChantierId);

    List<TemplateChantierTache> findByTemplateTacheId(Long templateTacheId);

    boolean existsByTemplateChantierIdAndTemplateTacheId(Long templateChantierId, Long templateTacheId);
}

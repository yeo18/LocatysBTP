package com.cms.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.enums.Priorite;
import com.cms.template.entity.TemplateTache;

/**
 * AccÃ¨s aux donnÃ©es des templates de tÃ¢ches.
 */
public interface TemplateTacheRepository extends JpaRepository<TemplateTache, Long> {

    List<TemplateTache> findByPriorite(Priorite priorite);

    List<TemplateTache> findByTitreContainingIgnoreCase(String titre);
}

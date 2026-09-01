package com.cms.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;

/**
 * AccÃ¨s aux donnÃ©es des templates de chantier.
 */
public interface TemplateChantierRepository extends JpaRepository<TemplateChantier, Long> {

    boolean existsByNom(String nom);

    List<TemplateChantier> findByStatut(TemplateChantierStatut statut);

    List<TemplateChantier> findByNomContainingIgnoreCase(String nom);
}

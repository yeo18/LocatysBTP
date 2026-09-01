package com.cms.chantier.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;

/**
 * AccÃ¨s aux donnÃ©es des chantiers.
 */
public interface ChantierRepository extends JpaRepository<Chantier, Long> {

    Page<Chantier> findByStatut(ChantierStatut statut, Pageable pageable);

    List<Chantier> findByResponsableId(Long responsableId);
}

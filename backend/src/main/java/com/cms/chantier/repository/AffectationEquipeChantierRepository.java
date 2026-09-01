package com.cms.chantier.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.equipe.entity.AffectationEquipeChantier;

/**
 * Accès aux affectations d'équipes à des chantiers.
 *
 * <p>Base de la sécurité des données : visibilité des chantiers par équipe.
 */
public interface AffectationEquipeChantierRepository extends JpaRepository<AffectationEquipeChantier, Long> {

    List<AffectationEquipeChantier> findByChantierId(Long chantierId);

    List<AffectationEquipeChantier> findByEquipeId(Long equipeId);

    List<AffectationEquipeChantier> findByEquipeIdIn(Collection<Long> equipeIds);
}

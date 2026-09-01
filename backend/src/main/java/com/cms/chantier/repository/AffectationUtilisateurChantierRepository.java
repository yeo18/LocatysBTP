package com.cms.chantier.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.chantier.entity.AffectationUtilisateurChantier;

/**
 * Accès aux affectations directes utilisateur <-> chantier (relation ternaire
 * Utilisateur x Chantier x Profil, avec période de validité).
 */
public interface AffectationUtilisateurChantierRepository
        extends JpaRepository<AffectationUtilisateurChantier, Long> {

    List<AffectationUtilisateurChantier> findByChantierId(Long chantierId);

    List<AffectationUtilisateurChantier> findByUtilisateurId(Long utilisateurId);

    List<AffectationUtilisateurChantier> findByProfilId(Long profilId);

    List<AffectationUtilisateurChantier> findByUtilisateurIdAndChantierId(Long utilisateurId, Long chantierId);
}

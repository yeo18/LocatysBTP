package com.cms.chantier.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.chantier.entity.UtilisateurPermissionChantier;

/**
 * Accès aux exceptions RBAC scopées par chantier.
 */
public interface UtilisateurPermissionChantierRepository
        extends JpaRepository<UtilisateurPermissionChantier, Long> {

    List<UtilisateurPermissionChantier> findByUtilisateurId(Long utilisateurId);

    List<UtilisateurPermissionChantier> findByUtilisateurIdAndType(Long utilisateurId,
            com.cms.utilisateur.entity.enums.UtilisateurPermissionType type);

    List<UtilisateurPermissionChantier> findByChantierId(Long chantierId);

    List<UtilisateurPermissionChantier> findByChantierIdAndUtilisateurId(Long chantierId, Long utilisateurId);

    Optional<UtilisateurPermissionChantier> findByUtilisateurIdAndChantierIdAndPermissionIdAndType(
            Long utilisateurId, Long chantierId, Long permissionId,
            com.cms.utilisateur.entity.enums.UtilisateurPermissionType type);
}
package com.cms.utilisateur.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.utilisateur.entity.UtilisateurPermission;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

/**
 * AccÃ¨s aux exceptions RBAC individuelles.
 */
public interface UtilisateurPermissionRepository extends JpaRepository<UtilisateurPermission, Long> {

    List<UtilisateurPermission> findByUtilisateurId(Long utilisateurId);

    List<UtilisateurPermission> findByPermissionId(Long permissionId);

    Optional<UtilisateurPermission> findByUtilisateurIdAndPermissionIdAndType(Long utilisateurId, Long permissionId,
            UtilisateurPermissionType type);
}

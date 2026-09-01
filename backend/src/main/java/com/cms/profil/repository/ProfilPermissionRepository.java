package com.cms.profil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.profil.entity.ProfilPermission;

/**
 * Accès aux permissions par défaut d'un profil (RBAC).
 */
public interface ProfilPermissionRepository extends JpaRepository<ProfilPermission, Long> {

    List<ProfilPermission> findByProfilId(Long profilId);

    Optional<ProfilPermission> findByProfilIdAndPermissionId(Long profilId, Long permissionId);

    List<ProfilPermission> findByPermissionId(Long permissionId);
}

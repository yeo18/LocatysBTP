package com.cms.permission.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.permission.entity.Permission;

/**
 * Accès aux données des permissions.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByNomPermission(String nomPermission);

    boolean existsByNomPermission(String nomPermission);

    List<Permission> findByModule(String module);
}

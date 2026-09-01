package com.cms.profil.entity;

import com.cms.permission.entity.Permission;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité associative Profil ↔ Permission (RBAC).
 *
 * <p>Table : {@code profil_permission} (MLD). Attribution dynamique des
 * permissions par défaut d'un profil.
 */
@Entity
@Table(name = "profil_permission",
        uniqueConstraints = @UniqueConstraint(name = "uk_profil_permission", columnNames = {"profil_id", "permission_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profil_id", nullable = false)
    private Profil profil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}

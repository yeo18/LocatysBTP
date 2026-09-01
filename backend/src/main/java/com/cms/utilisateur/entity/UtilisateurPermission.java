package com.cms.utilisateur.entity;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import java.time.LocalDateTime;

import com.cms.permission.entity.Permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * EntitÃ© associative des exceptions RBAC individuelles.
 *
 * <p>Table : {@code utilisateur_permission} (MLD). Type ACCORDER/REFUSER
 * (REFUSER prioritaire). {@code createdBy} trace l'auteur de la dÃ©cision.
 */
@Entity
@Table(name = "utilisateur_permission",
        uniqueConstraints = @UniqueConstraint(name = "uk_utilisateur_permission", columnNames = {"utilisateur_id", "permission_id", "type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UtilisateurPermissionType type;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Utilisateur createdBy;
}

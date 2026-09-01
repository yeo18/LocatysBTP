package com.cms.chantier.entity;

import java.time.LocalDateTime;

import com.cms.permission.entity.Permission;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

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
 * Exception RBAC scopÃ©e Ã  un chantier.
 *
 * <p>Table : {@code utilisateur_permission_chantier} (V11). Identique Ã 
 * {@code utilisateur_permission} mais porte un {@code chantier_id} : la
 * permission ACCORDER/REFUSER ne s'applique qu'au pÃ©rimÃ¨tre de ce chantier.
 *
 * <p>Cas d'usage : un utilisateur peut Ãªtre Â« chef Â» du chantier A (on lui
 * accorde {@code CHANTIER_MODIFIER} sur le chantier A) sans l'Ãªtre du
 * chantier B.
 */
@Entity
@Table(name = "utilisateur_permission_chantier",
        uniqueConstraints = @UniqueConstraint(name = "uk_utilisateur_permission_chantier",
                columnNames = {"utilisateur_id", "chantier_id", "permission_id", "type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurPermissionChantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Chantier chantier;

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
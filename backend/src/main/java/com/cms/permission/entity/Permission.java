package com.cms.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant une permission (unité du RBAC).
 *
 * <p>Table : {@code permission} (MLD). Nom technique unique
 * ({@code nomPermission}, ex : TACHE_VIEW).
 */
@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "nom_permission", nullable = false, unique = true, length = 100)
    private String nomPermission;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(length = 255)
    private String description;
}

package com.cms.chantier.dto;

import java.time.LocalDateTime;

import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une exception RBAC scopee a un chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurPermissionChantierResponse {

    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private Long chantierId;
    private String chantierNom;
    private Long permissionId;
    private String nomPermission;
    private String permissionNom;
    private UtilisateurPermissionType type;
    private Long createdById;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
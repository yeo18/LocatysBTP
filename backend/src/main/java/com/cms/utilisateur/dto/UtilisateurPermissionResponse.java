package com.cms.utilisateur.dto;

import java.time.LocalDateTime;

import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une exception RBAC individuelle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurPermissionResponse {

    private Long id;
    private Long utilisateurId;
    private Long permissionId;
    private String nomPermission;
    private UtilisateurPermissionType type;
    private Long createdById;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

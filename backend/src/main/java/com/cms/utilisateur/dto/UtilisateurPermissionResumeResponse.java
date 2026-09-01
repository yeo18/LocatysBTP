package com.cms.utilisateur.dto;

import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une exception RBAC individuelle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurPermissionResumeResponse {

    private Long id;
    private String nomPermission;
    private UtilisateurPermissionType type;
}

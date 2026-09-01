package com.cms.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une permission RBAC.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String nom;
    private String nomPermission;
    private String module;
    private String description;
}

package com.cms.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une permission RBAC.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResumeResponse {

    private Long id;
    private String nom;
    private String nomPermission;
    private String module;
}

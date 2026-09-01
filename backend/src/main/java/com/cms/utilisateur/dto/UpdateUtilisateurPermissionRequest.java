package com.cms.utilisateur.dto;

import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'une exception RBAC individuelle.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtilisateurPermissionRequest {

    @NotNull
    private Long permissionId;

    @NotNull
    private UtilisateurPermissionType type;
}

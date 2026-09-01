package com.cms.utilisateur.dto;

import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une exception RBAC individuelle (ACCORDER/REFUSER).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUtilisateurPermissionRequest {

    @NotNull
    private Long utilisateurId;

    @NotNull
    private Long permissionId;

    @NotNull
    private UtilisateurPermissionType type;
}

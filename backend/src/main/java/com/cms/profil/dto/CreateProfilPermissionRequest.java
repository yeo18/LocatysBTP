package com.cms.profil.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'une association Profil - Permission.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfilPermissionRequest {

    @NotNull
    private Long profilId;

    @NotNull
    private Long permissionId;
}

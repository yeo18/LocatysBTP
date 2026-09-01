package com.cms.profil.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'une association Profil - Permission.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfilPermissionRequest {

    @NotNull
    private Long permissionId;
}

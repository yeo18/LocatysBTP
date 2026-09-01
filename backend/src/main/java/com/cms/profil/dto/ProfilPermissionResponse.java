package com.cms.profil.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une association Profil - Permission.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPermissionResponse {

    private Long id;
    private Long profilId;
    private String profilNom;
    private Long permissionId;
    private String nomPermission;
    private String permissionNom;
}

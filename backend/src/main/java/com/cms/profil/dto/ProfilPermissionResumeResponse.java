package com.cms.profil.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'une association Profil - Permission.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPermissionResumeResponse {

    private Long id;
    private String nomPermission;
}

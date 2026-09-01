package com.cms.profil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'un profil (role RBAC).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfilRequest {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @Size(max = 255)
    private String description;
}

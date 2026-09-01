package com.cms.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'une permission RBAC.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionRequest {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[A-Z_]+$", message = "Le nom doit etre en MAJUSCULES et underscore")
    private String nomPermission;

    @NotBlank
    @Size(max = 50)
    private String module;

    @Size(max = 255)
    private String description;
}

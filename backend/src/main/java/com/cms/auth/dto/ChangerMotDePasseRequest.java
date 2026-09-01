package com.cms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de changement de mot de passe par l'utilisateur courant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangerMotDePasseRequest {

    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, max = 255, message = "Le nouveau mot de passe doit contenir au moins 8 caracteres")
    private String nouveauMotDePasse;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmation;
}

package com.cms.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de creation d'un utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUtilisateurRequest {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    @Size(max = 100)
    private String prenom;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 255)
    private String password;

    @Size(max = 20)
    @Pattern(regexp = "^[0-9+ .-]*$", message = "Numero de telephone invalide")
    private String telephone;

    private Long profilId;
}

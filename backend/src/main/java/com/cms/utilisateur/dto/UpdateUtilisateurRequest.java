package com.cms.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de mise a jour d'un utilisateur.
 *
 * <p>Le mot de passe n'est pas modifie via cette requete (processus dedie).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtilisateurRequest {

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

    @Size(max = 20)
    @Pattern(regexp = "^[0-9+ .-]*$", message = "Numero de telephone invalide")
    private String telephone;

    @NotNull
    private Long profilId;
}

package com.cms.chantier.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete d'affectation d'un utilisateur a un chantier (relation ternaire).
 *
 * <p>Le {@code profilId} est optionnel : s'il est absent, le profil global de
 * l'utilisateur est repris. La periode ({@code dateDebut}, {@code dateFin}
 * exclue) garantit un profil unique par periode sur le chantier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAffectationUtilisateurChantierRequest {

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long utilisateurId;

    private Long profilId;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;
}
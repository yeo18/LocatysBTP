package com.cms.utilisateur.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de changement de profil (role) d'un utilisateur.
 *
 * <p>Operation administrative uniquement (jamais utilisee par
 * {@code POST /auth/register}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierProfilRequest {

    @NotNull(message = "Le profil est obligatoire.")
    private Long profilId;
}
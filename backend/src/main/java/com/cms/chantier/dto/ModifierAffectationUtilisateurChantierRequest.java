package com.cms.chantier.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Requete de modification d'une affectation utilisateur <-> chantier :
 * changement de profil et/ou de periode de validite.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierAffectationUtilisateurChantierRequest {

    private Long profilId;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;
}
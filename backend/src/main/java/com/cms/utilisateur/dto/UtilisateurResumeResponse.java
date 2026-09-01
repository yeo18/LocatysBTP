package com.cms.utilisateur.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un utilisateur (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResumeResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String profilNom;
}

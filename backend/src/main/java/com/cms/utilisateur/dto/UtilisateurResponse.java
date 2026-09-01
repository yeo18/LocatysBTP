package com.cms.utilisateur.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un utilisateur. Ne contient jamais le mot de passe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Long profilId;
    private String profilNom;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

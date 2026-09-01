package com.cms.chantier.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse d'une affectation utilisateur <-> chantier (relation ternaire) :
 * utilisateur, chantier, profil choisi et periode de validite.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationUtilisateurChantierResponse {

    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private Long chantierId;
    private String chantierNom;
    private Long profilId;
    private String profilNom;
    private LocalDateTime dateAffectation;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
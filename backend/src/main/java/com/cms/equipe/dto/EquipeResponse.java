package com.cms.equipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'une equipe.
 *
 * <p>Contient uniquement les attributs valides du dictionnaire
 * (dictionnaire-donnees-valide.md 6.7 EQUIPE) exposes a l'exterieur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipeResponse {

    private Long id;
    private String nom;
    private String description;
}

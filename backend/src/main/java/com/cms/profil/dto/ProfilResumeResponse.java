package com.cms.profil.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse resume d'un profil (listes et selecteurs).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResumeResponse {

    private Long id;
    private String nom;
}

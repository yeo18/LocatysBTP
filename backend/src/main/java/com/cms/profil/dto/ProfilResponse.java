package com.cms.profil.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un profil.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

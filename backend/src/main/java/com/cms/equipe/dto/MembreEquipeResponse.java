package com.cms.equipe.dto;

import java.time.LocalDate;

import com.cms.equipe.entity.enums.RoleDansEquipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse detaillee d'un membre d'equipe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembreEquipeResponse {

    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private Long equipeId;
    private String equipeNom;
    private RoleDansEquipe roleDansEquipe;
    private LocalDate dateIntegration;
}

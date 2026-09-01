package com.cms.template.dto;

import com.cms.tache.entity.enums.AffectationTacheRole;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Affectation d'une tache creee lors de l'import d'un TemplateChantier.
 *
 * <p>L'import cree la tache parente (copie du {@code TemplateTache}) puis une
 * tache par tache structuree ({@code TemplateTacheTache}) du template. Une
 * affectation cible soit la tache parente (via {@code templateTacheId}) soit
 * une sous-tache precise (via {@code templateTacheTacheId}) â€” au moins l'un
 * des deux doit etre renseigne (verifie par {@link #isCibleValide()}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportTemplateChantierAffectationRequest {

    private Long templateTacheId;

    private Long templateTacheTacheId;

    @NotNull
    private Long utilisateurId;

    @NotNull
    private AffectationTacheRole role;

    @AssertTrue(message = "Une affectation d'import doit cibler un TemplateTache ou une tache structuree")
    public boolean isCibleValide() {
        return templateTacheId != null || templateTacheTacheId != null;
    }
}
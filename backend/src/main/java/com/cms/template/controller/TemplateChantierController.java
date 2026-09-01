package com.cms.template.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;
import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.ImportTemplateChantierAffectationRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.service.TemplateChantierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module Template — TemplateChantier (LOOP 5.10).
 *
 * <p>Le Controller est une simple couche de transport : il delege
 * integralement au Service (aucune logique metier, aucun acces Repository).
 * Autorisation fonctionnelle RBAC (Niveau 1) via {@code @PreAuthorize}
 * (permissions {@code TEMPLATE_CHANTIER_*}) ; la restriction des donnees
 * (Niveau 2, perimetre chantier) est appliquee dans la couche Service.
 */
@RestController
@RequestMapping(ApiRoutes.TEMPLATE_CHANTIERS)
@Tag(name = "Templates Chantiers", description = "Gestion des templates de chantier et de leurs imports")
public class TemplateChantierController {

    private final TemplateChantierService templateChantierService;

    public TemplateChantierController(TemplateChantierService templateChantierService) {
        this.templateChantierService = templateChantierService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','CREER')")
    @Operation(summary = "Creer un template de chantier", description = "Cree un TemplateChantier (statut ACTIF par defaut).")
    public ResponseEntity<ApiResponse<TemplateChantierResponse>> creer(
            @Valid @RequestBody CreateTemplateChantierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("TemplateChantier cree", templateChantierService.creer(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','LIRE')")
    @Operation(summary = "Lister les templates de chantier",
            description = "Liste tous les TemplateChantier (actifs et inactifs). Optionnellement filtre par nom (recherche partielle, insensible a la casse).")
    public ResponseEntity<ApiResponse<List<TemplateChantierResponse>>> lister(
            @RequestParam(required = false) String nom) {
        if (nom != null && !nom.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(templateChantierService.listerParNom(nom)));
        }
        return ResponseEntity.ok(ApiResponse.success(templateChantierService.lister()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','LIRE')")
    @Operation(summary = "Consulter un template de chantier", description = "Detail d'un TemplateChantier par identifiant.")
    public ResponseEntity<ApiResponse<TemplateChantierResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(templateChantierService.trouverParId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','MODIFIER')")
    @Operation(summary = "Modifier un template de chantier", description = "Met a jour un TemplateChantier (statut remis a ACTIF).")
    public ResponseEntity<ApiResponse<TemplateChantierResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateChantierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("TemplateChantier modifie",
                templateChantierService.modifier(id, request)));
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','MODIFIER')")
    @Operation(summary = "Desactiver un template de chantier", description = "Passe un TemplateChantier au statut INACTIF.")
    public ResponseEntity<ApiResponse<TemplateChantierResponse>> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("TemplateChantier desactive",
                templateChantierService.desactiver(id)));
    }

    // ------------------------------------------------------------------
    // Association TemplateChantier <-> TemplateTache
    // ------------------------------------------------------------------

    @PostMapping("/{templateChantierId}/taches/{templateTacheId}")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','MODIFIER')")
    @Operation(summary = "Associer un template de tache", description = "Associe un TemplateTache a un TemplateChantier.")
    public ResponseEntity<ApiResponse<Void>> associerTemplateTache(
            @PathVariable Long templateChantierId,
            @PathVariable Long templateTacheId) {
        templateChantierService.associerTemplateTache(templateChantierId, templateTacheId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("TemplateTache associe", null));
    }

    @DeleteMapping("/{templateChantierId}/taches/{templateTacheId}")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','MODIFIER')")
    @Operation(summary = "Retirer un template de tache", description = "Retire l'association d'un TemplateTache a un TemplateChantier.")
    public ResponseEntity<ApiResponse<Void>> retirerTemplateTache(
            @PathVariable Long templateChantierId,
            @PathVariable Long templateTacheId) {
        templateChantierService.retirerTemplateTache(templateChantierId, templateTacheId);
        return ResponseEntity.ok(ApiResponse.success("TemplateTache retire", null));
    }

    @GetMapping("/{templateChantierId}/taches")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','LIRE')")
    @Operation(summary = "TemplateTache associes", description = "Liste ordonnee des TemplateTache associes a un TemplateChantier.")
    public ResponseEntity<ApiResponse<List<TemplateTacheResumeResponse>>> trouverTachesAssociees(
            @PathVariable Long templateChantierId) {
        return ResponseEntity.ok(ApiResponse.success(
                templateChantierService.trouverTachesAssociees(templateChantierId)));
    }

    // ------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------

    @PostMapping("/{templateChantierId}/import")
    @PreAuthorize("hasPermission('TEMPLATE_CHANTIER','LIRE')")
    @Operation(summary = "Importer un template de chantier",
            description = "Copie chaque TemplateTache associe en une nouvelle Tache du chantier cible (snapshot). "
                    + "Le corps (optionnel) peut contenir les affectations : [{\"templateTacheId\":1,"
                    + "\"utilisateurId\":2,\"role\":\"REALISATEUR\"}] pour attribuer chaque tache creee.")
    public ResponseEntity<ApiResponse<Integer>> importerDansChantier(
            @PathVariable Long templateChantierId,
            @RequestParam Long chantierId,
            @RequestBody(required = false) List<ImportTemplateChantierAffectationRequest> affectations) {
        int creees = templateChantierService.importerDansChantier(templateChantierId, chantierId, affectations);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creees + " tache(s) creee(s)", creees));
    }

}

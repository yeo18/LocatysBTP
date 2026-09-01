package com.cms.template.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.CreateTemplateTacheTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.dto.UpdateTemplateTacheTacheRequest;
import com.cms.template.service.TemplateTacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module Template — TemplateTache (LOOP 5.10).
 *
 * <p>Le Controller delege integralement au Service. Le modele TemplateTache
 * ne possede PAS de statut : aucun endpoint de desactivation/activation.
 * Autorisation fonctionnelle RBAC (Niveau 1) via {@code @PreAuthorize}
 * (permissions {@code TEMPLATE_TACHE_*}) ; la restriction des donnees
 * (Niveau 2, perimetre chantier) est appliquee dans la couche Service.
 */
@RestController
@RequestMapping(ApiRoutes.TEMPLATE_TACHES)
@Tag(name = "Templates Taches", description = "Gestion des templates de taches et de leurs imports")
public class TemplateTacheController {

    private final TemplateTacheService templateTacheService;

    public TemplateTacheController(TemplateTacheService templateTacheService) {
        this.templateTacheService = templateTacheService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','CREER')")
    @Operation(summary = "Creer un template de tache", description = "Cree un TemplateTache (groupe reutilisable de taches).")
    public ResponseEntity<ApiResponse<TemplateTacheResponse>> creer(
            @Valid @RequestBody CreateTemplateTacheRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("TemplateTache cree", templateTacheService.creer(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','LIRE')")
    @Operation(summary = "Lister les templates de taches",
            description = "Liste tous les TemplateTache. Optionnellement filtre par nom (recherche partielle, insensible a la casse).")
    public ResponseEntity<ApiResponse<List<TemplateTacheResponse>>> lister(
            @RequestParam(required = false) String nom) {
        if (nom != null && !nom.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(templateTacheService.listerParNom(nom)));
        }
        return ResponseEntity.ok(ApiResponse.success(templateTacheService.lister()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','LIRE')")
    @Operation(summary = "Consulter un template de tache", description = "Detail d'un TemplateTache par identifiant.")
    public ResponseEntity<ApiResponse<TemplateTacheResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(templateTacheService.trouverParId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','MODIFIER')")
    @Operation(summary = "Modifier un template de tache", description = "Met a jour un TemplateTache.")
    public ResponseEntity<ApiResponse<TemplateTacheResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateTacheRequest request) {
        return ResponseEntity.ok(ApiResponse.success("TemplateTache modifie",
                templateTacheService.modifier(id, request)));
    }

    // ------------------------------------------------------------------
    // Taches structurees d'un TemplateTache (LOOP §1, §2, §18)
    // ------------------------------------------------------------------

    @GetMapping("/{templateTacheId}/taches")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','LIRE')")
    @Operation(summary = "Taches structurees d'un template",
            description = "Liste ordonnee des taches structurees d'un TemplateTache.")
    public ResponseEntity<ApiResponse<List<TemplateTacheTacheResumeResponse>>> listerTaches(
            @PathVariable Long templateTacheId) {
        return ResponseEntity.ok(ApiResponse.success(
                templateTacheService.listerTaches(templateTacheId)));
    }

    @PostMapping("/{templateTacheId}/taches")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','MODIFIER')")
    @Operation(summary = "Ajouter une tache structuree",
            description = "Ajoute une tache au TemplateTache.")
    public ResponseEntity<ApiResponse<TemplateTacheTacheResponse>> ajouterTache(
            @PathVariable Long templateTacheId,
            @Valid @RequestBody CreateTemplateTacheTacheRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tache ajoutee",
                        templateTacheService.ajouterTache(templateTacheId, request)));
    }

    @PostMapping("/{templateTacheId}/taches/import")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','MODIFIER')")
    @Operation(summary = "Importer une tache existante",
            description = "Copie les champs d'une tache reelle en nouvelle tache structuree (snapshot).")
    public ResponseEntity<ApiResponse<TemplateTacheTacheResponse>> importerTache(
            @PathVariable Long templateTacheId,
            @RequestParam Long tacheId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tache importee",
                        templateTacheService.importerTache(templateTacheId, tacheId)));
    }

    @PutMapping("/{templateTacheId}/taches/{itemId}")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','MODIFIER')")
    @Operation(summary = "Modifier une tache structuree",
            description = "Met a jour une tache structuree du TemplateTache.")
    public ResponseEntity<ApiResponse<TemplateTacheTacheResponse>> modifierTache(
            @PathVariable Long templateTacheId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateTemplateTacheTacheRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tache modifiee",
                templateTacheService.modifierTache(templateTacheId, itemId, request)));
    }

    @DeleteMapping("/{templateTacheId}/taches/{itemId}")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','MODIFIER')")
    @Operation(summary = "Retirer une tache structuree",
            description = "Retire une tache structuree du TemplateTache.")
    public ResponseEntity<ApiResponse<Void>> retirerTache(
            @PathVariable Long templateTacheId,
            @PathVariable Long itemId) {
        templateTacheService.retirerTache(templateTacheId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Tache retiree", null));
    }

    // ------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------

    @PostMapping("/{templateTacheId}/import")
    @PreAuthorize("hasPermission('TEMPLATE_TACHE','LIRE')")
    @Operation(summary = "Importer un template de tache",
            description = "Copie un TemplateTache en une nouvelle Tache du chantier cible (snapshot).")
    public ResponseEntity<ApiResponse<Integer>> importerDansChantier(
            @PathVariable Long templateTacheId,
            @RequestParam Long chantierId) {
        int creees = templateTacheService.importerDansChantier(templateTacheId, chantierId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creees + " tache(s) creee(s)", creees));
    }

}

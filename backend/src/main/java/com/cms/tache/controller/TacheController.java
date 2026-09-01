package com.cms.tache.controller;

import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;
import com.cms.common.response.PageResponse;
import com.cms.tache.dto.AffectationTacheResponse;
import com.cms.tache.dto.CreateAffectationTacheRequest;
import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.TacheResumeResponse;
import com.cms.tache.dto.UpdateAffectationTacheRequest;
import com.cms.tache.dto.UpdateTacheRequest;
import com.cms.tache.dto.ValidationTacheResponse;
import com.cms.tache.entity.enums.ValidationTacheStatut;
import com.cms.tache.service.AffectationTacheService;
import com.cms.tache.service.TacheService;
import com.cms.tache.service.ValidationTacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module tache (LOOP 3.15).
 *
 * <p>Le Controller verifie l'autorisation fonctionnelle (RBAC Niveau 1 via
 * {@code @PreAuthorize}). Les permissions referencees ({@code TACHE_CREER},
 * {@code TACHE_LIRE}, {@code TACHE_MODIFIER}, {@code TACHE_SUPPRIMER}) sont
 * utilisees uniquement si elles existent dans le referentiel. La restriction
 * des donnees (Niveau 2) est appliquee dans la couche Service.
 */
@RestController
@RequestMapping(ApiRoutes.TACHES)
@Tag(name = "Taches", description = "Gestion des taches, des affectations et des validations")
public class TacheController {

    private final TacheService tacheService;
    private final AffectationTacheService affectationTacheService;
    private final ValidationTacheService validationTacheService;

    public TacheController(TacheService tacheService,
                           AffectationTacheService affectationTacheService,
                           ValidationTacheService validationTacheService) {
        this.tacheService = tacheService;
        this.affectationTacheService = affectationTacheService;
        this.validationTacheService = validationTacheService;
    }

    // ------------------------------------------------------------------
    // Tache (CRUD)
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasPermission('TACHE','CREER')")
    @Operation(summary = "Creer une tache", description = "Cree une tache (statut A_FAIRE, progression 0).")
    public ResponseEntity<ApiResponse<TacheResponse>> createTache(
            @Valid @RequestBody CreateTacheRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tache creee", tacheService.createTache(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('TACHE','LIRE')")
    @Operation(summary = "Lister les taches", description = "Liste paginee des taches accessibles.")
    public ResponseEntity<ApiResponse<PageResponse<TacheResumeResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(tacheService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('TACHE','LIRE')")
    @Operation(summary = "Consulter une tache", description = "Detail d'une tache par identifiant.")
    public ResponseEntity<ApiResponse<TacheResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tacheService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('TACHE','MODIFIER')")
    @Operation(summary = "Modifier une tache", description = "Met a jour une tache (titre, description, priorite, statut, progression, dates).")
    public ResponseEntity<ApiResponse<TacheResponse>> updateTache(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTacheRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tache modifiee", tacheService.updateTache(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('TACHE','SUPPRIMER')")
    @Operation(summary = "Supprimer une tache", description = "Supprime une tache.")
    public ResponseEntity<ApiResponse<Void>> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return ResponseEntity.ok(ApiResponse.success("Tache supprimee", null));
    }

    // ------------------------------------------------------------------
    // Affectations
    // ------------------------------------------------------------------

    @PostMapping("/affectations")
    @PreAuthorize("hasPermission('TACHE','MODIFIER')")
    @Operation(summary = "Affecter une tache", description = "Affecte une tache a un utilisateur ou une equipe (REALISATEUR/CONTROLEUR).")
    public ResponseEntity<ApiResponse<AffectationTacheResponse>> assignTache(
            @Valid @RequestBody CreateAffectationTacheRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tache affectee", affectationTacheService.assignTache(request)));
    }

    @PutMapping("/affectations/{id}")
    @PreAuthorize("hasPermission('TACHE','MODIFIER')")
    @Operation(summary = "Modifier une affectation", description = "Change la cible ou le role d'une affectation.")
    public ResponseEntity<ApiResponse<AffectationTacheResponse>> updateAffectation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAffectationTacheRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Affectation modifiee",
                affectationTacheService.updateAffectation(id, request)));
    }

    @DeleteMapping("/affectations/{id}")
    @PreAuthorize("hasPermission('TACHE','SUPPRIMER')")
    @Operation(summary = "Retirer une affectation", description = "Retire une affectation de tache.")
    public ResponseEntity<ApiResponse<Void>> removeAffectation(@PathVariable Long id) {
        affectationTacheService.removeAffectation(id);
        return ResponseEntity.ok(ApiResponse.success("Affectation retiree", null));
    }

    @GetMapping("/{tacheId}/affectations")
    @PreAuthorize("hasPermission('TACHE','LIRE')")
    @Operation(summary = "Lister les affectations d'une tache", description = "Utilisateurs et equipes affectes a la tache (REALISATEUR / CONTROLEUR).")
    public ResponseEntity<ApiResponse<java.util.List<AffectationTacheResponse>>> getAffectations(
            @PathVariable Long tacheId) {
        return ResponseEntity.ok(ApiResponse.success(affectationTacheService.findByTacheId(tacheId)));
    }

    // ------------------------------------------------------------------
    // Validations
    // ------------------------------------------------------------------

    @PostMapping("/{tacheId}/valider")
    @PreAuthorize("hasPermission('TACHE','VALIDER')")
    @Operation(summary = "Valider une tache", description = "Valide les travaux (statut VALIDE, progression 100). Permission requise : TACHE_VALIDER.")
    public ResponseEntity<ApiResponse<ValidationTacheResponse>> validerTache(
            @PathVariable Long tacheId,
            @Valid @RequestBody CreateValidationTacheRequest request) {
        request.setTacheId(tacheId);
        request.setStatut(ValidationTacheStatut.VALIDE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Validation enregistree",
                        validationTacheService.validateTache(request)));
    }

    @PostMapping("/{tacheId}/refuser")
    @PreAuthorize("hasPermission('TACHE','REFUSER')")
    @Operation(summary = "Refuser une tache", description = "Refuse une validation (verification terrain : la tache n'est pas conforme). Permission requise : TACHE_REFUSER.")
    public ResponseEntity<ApiResponse<ValidationTacheResponse>> refuserTache(
            @PathVariable Long tacheId,
            @Valid @RequestBody CreateValidationTacheRequest request) {
        request.setTacheId(tacheId);
        request.setStatut(ValidationTacheStatut.REFUSE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refus enregistre",
                        validationTacheService.validateTache(request)));
    }

    @GetMapping("/{tacheId}/validations")
    @PreAuthorize("hasPermission('TACHE','LIRE')")
    @Operation(summary = "Historique des validations", description = "Historique des decisions de validation d'une tache.")
    public ResponseEntity<ApiResponse<java.util.List<ValidationTacheResponse>>> getValidationHistory(
            @PathVariable Long tacheId) {
        return ResponseEntity.ok(ApiResponse.success(validationTacheService.getValidationHistory(tacheId)));
    }

}

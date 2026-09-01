package com.cms.chantier.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cms.chantier.dto.AffectationUtilisateurChantierResponse;
import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.ChantierResumeResponse;
import com.cms.chantier.dto.ConfirmerLocalisationRequest;
import com.cms.chantier.dto.CreateAffectationUtilisateurChantierRequest;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.dto.ModifierAffectationUtilisateurChantierRequest;
import com.cms.chantier.dto.UpdateChantierRequest;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.service.AffectationUtilisateurChantierService;
import com.cms.chantier.service.ChantierService;
import com.cms.common.constants.ApiRoutes;
import com.cms.common.dto.SearchRequest;
import com.cms.common.response.ApiResponse;
import com.cms.common.response.PageResponse;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module chantier.
 *
 * <p>Le Controller verifie uniquement l'autorisation fonctionnelle (RBAC
 * Niveau 1 via {@code @PreAuthorize}). Les permissions referencees
 * ({@code CHANTIER_CREER}, {@code CHANTIER_LIRE}, {@code CHANTIER_MODIFIER},
 * {@code CHANTIER_SUPPRIMER}) existent dans le referentiel : aucune
 * permission n'est creee ici.
 */
@RestController
@RequestMapping(ApiRoutes.CHANTIERS)
@Tag(name = "Chantiers", description = "Gestion des chantiers de construction")
public class ChantierController {

    private final ChantierService chantierService;
    private final AffectationUtilisateurChantierService affectationUtilisateurChantierService;

    public ChantierController(ChantierService chantierService,
                              AffectationUtilisateurChantierService affectationUtilisateurChantierService) {
        this.chantierService = chantierService;
        this.affectationUtilisateurChantierService = affectationUtilisateurChantierService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('CHANTIER','CREER')")
    @Operation(summary = "Creer un chantier", description = "Cree un chantier (statut PREVU par defaut, progression 0).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Chantier cree"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requete invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifie"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission CHANTIER_CREER requise")
    })
    public ResponseEntity<ApiResponse<ChantierResponse>> creer(
            @Valid @RequestBody CreateChantierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chantier cree", chantierService.creer(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('CHANTIER','LIRE')")
    @Operation(summary = "Rechercher des chantiers", description = "Recherche paginee de chantiers avec filtre statut optionnel.")
    public ResponseEntity<ApiResponse<PageResponse<ChantierResumeResponse>>> rechercher(
            @ModelAttribute SearchRequest search,
            @RequestParam(required = false) ChantierStatut statut) {
        Page<ChantierResumeResponse> page = chantierService.rechercher(search, statut);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'LIRE')")
    @Operation(summary = "Consulter un chantier", description = "Detail d'un chantier par identifiant.")
    public ResponseEntity<ApiResponse<ChantierResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(chantierService.trouverParId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'MODIFIER')")
    @Operation(summary = "Modifier un chantier", description = "Met a jour les informations d'un chantier. Autorise si l'utilisateur possede CHANTIER_MODIFIER (global OU scoped) pour CE chantier precis.")
    public ResponseEntity<ApiResponse<ChantierResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateChantierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Chantier modifie",
                chantierService.modifier(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'SUPPRIMER')")
    @Operation(summary = "Annuler un chantier", description = "Suppression logique : passe le chantier au statut ANNULE. Autorise si CHANTIER_SUPPRIMER (global OU scoped) pour CE chantier.")
    public ResponseEntity<ApiResponse<ChantierResponse>> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chantier annule",
                chantierService.annuler(id)));
    }

    // ------------------------------------------------------------------
    // Localisation gÃ©ographique du chantier
    // ------------------------------------------------------------------

    @PutMapping("/{id}/localisation")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'MODIFIER')")
    @Operation(summary = "Confirmer la position du chantier", description = "Enregistre la position officielle (geocodage ou carte) utilisee pour l'analyse du site.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Position confirmee"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Coordonnees invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission CHANTIER_MODIFIER requise")
    })
    public ResponseEntity<ApiResponse<ChantierResponse>> confirmerLocalisation(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmerLocalisationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Position confirmee",
                chantierService.confirmerLocalisation(id, request)));
    }

    // ------------------------------------------------------------------
    // Affectations directes utilisateur <-> chantier
    // ------------------------------------------------------------------

    @GetMapping("/{id}/utilisateurs")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'LIRE')")
    @Operation(summary = "Lister les utilisateurs d'un chantier", description = "Utilisateurs affectes directement a ce chantier (candidats des equipes).")
    public ResponseEntity<ApiResponse<List<UtilisateurResumeResponse>>> listerUtilisateurs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                affectationUtilisateurChantierService.listerUtilisateursDuChantier(id)));
    }

    @GetMapping("/{id}/affectations")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'LIRE')")
    @Operation(summary = "Lister les affectations d'un chantier", description = "Affectations utilisateur <-> chantier avec leur profil et leur periode de validite (relation ternaire).")
    public ResponseEntity<ApiResponse<List<AffectationUtilisateurChantierResponse>>> listerAffectations(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                affectationUtilisateurChantierService.listerAffectationsDuChantier(id)));
    }

    @PostMapping("/{id}/utilisateurs")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'MODIFIER')")
    @Operation(summary = "Affecter un utilisateur a un chantier", description = "Affecte un utilisateur au chantier avec un profil unique pour la periode (le profil global est repris si non precise).")
    public ResponseEntity<ApiResponse<AffectationUtilisateurChantierResponse>> affecterUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody CreateAffectationUtilisateurChantierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Utilisateur affecte au chantier",
                        affectationUtilisateurChantierService.affecter(id, request)));
    }

    @PatchMapping("/affectations/{affectationId}")
    @PreAuthorize("hasPermission('CHANTIER', 'MODIFIER')")
    @Operation(summary = "Modifier une affectation", description = "Change le profil et/ou la periode de validite d'une affectation utilisateur <-> chantier.")
    public ResponseEntity<ApiResponse<AffectationUtilisateurChantierResponse>> modifierAffectation(
            @PathVariable Long affectationId,
            @Valid @RequestBody ModifierAffectationUtilisateurChantierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Affectation modifiee",
                affectationUtilisateurChantierService.modifier(affectationId, request)));
    }

    @DeleteMapping("/{id}/utilisateurs/{utilisateurId}")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'MODIFIER')")
    @Operation(summary = "Retirer un utilisateur d'un chantier", description = "Retire un utilisateur de la liste des utilisateurs autorises du chantier.")
    public ResponseEntity<ApiResponse<Void>> retirerUtilisateur(
            @PathVariable Long id,
            @PathVariable Long utilisateurId) {
        affectationUtilisateurChantierService.retirer(id, utilisateurId);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur retire du chantier", null));
    }

    @DeleteMapping("/affectations/{affectationId}")
    @PreAuthorize("hasPermission('CHANTIER', 'MODIFIER')")
    @Operation(summary = "Supprimer une affectation", description = "Supprime une affectation par son identifiant.")
    public ResponseEntity<ApiResponse<Void>> supprimerAffectation(@PathVariable Long affectationId) {
        affectationUtilisateurChantierService.supprimer(affectationId);
        return ResponseEntity.ok(ApiResponse.success("Affectation supprimee", null));
    }

}

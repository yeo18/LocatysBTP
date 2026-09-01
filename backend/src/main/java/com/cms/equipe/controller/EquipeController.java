package com.cms.equipe.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;
import com.cms.equipe.dto.AffectationEquipeChantierResponse;
import com.cms.equipe.dto.AffectationEquipeChantierResumeResponse;
import com.cms.equipe.dto.CreateAffectationEquipeChantierRequest;
import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.CreateMembreEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.EquipeResumeResponse;
import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.dto.UpdateEquipeRequest;
import com.cms.equipe.dto.UpdateMembreEquipeRequest;
import com.cms.equipe.service.AffectationEquipeChantierService;
import com.cms.equipe.service.EquipeService;
import com.cms.equipe.service.MembreEquipeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module equipe.
 *
 * <p>Le Controller verifie uniquement l'autorisation fonctionnelle (RBAC
 * Niveau 1 via {@code @PreAuthorize}). Les permissions referencees
 * ({@code EQUIPE_CREER}, {@code EQUIPE_LIRE}, {@code EQUIPE_MODIFIER},
 * {@code EQUIPE_SUPPRIMER}) sont utilisees uniquement si elles existent
 * dans le referentiel : aucune permission n'est creee ici.
 */
@RestController
@RequestMapping(ApiRoutes.EQUIPES)
@Tag(name = "Equipes", description = "Gestion des equipes, des membres et des affectations chantiers")
public class EquipeController {

    private final EquipeService equipeService;
    private final MembreEquipeService membreEquipeService;
    private final AffectationEquipeChantierService affectationEquipeChantierService;
    private final com.cms.equipe.service.EquipeDetailService equipeDetailService;

    public EquipeController(EquipeService equipeService,
                            MembreEquipeService membreEquipeService,
                            AffectationEquipeChantierService affectationEquipeChantierService,
                            com.cms.equipe.service.EquipeDetailService equipeDetailService) {
        this.equipeService = equipeService;
        this.membreEquipeService = membreEquipeService;
        this.affectationEquipeChantierService = affectationEquipeChantierService;
        this.equipeDetailService = equipeDetailService;
    }

    // ------------------------------------------------------------------
    // Equipe (CRUD)
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasPermission('EQUIPE','CREER')")
    @Operation(summary = "Creer une equipe", description = "Cree une equipe active par defaut.")
    public ResponseEntity<ApiResponse<EquipeResponse>> creer(
            @Valid @RequestBody CreateEquipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Equipe creee", equipeService.creer(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Lister les equipes", description = "Liste des equipes.")
    public ResponseEntity<ApiResponse<List<EquipeResumeResponse>>> lister() {
        return ResponseEntity.ok(ApiResponse.success(equipeService.lister()));
    }

    @GetMapping("/detaillees")
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Lister les equipes detaillees", description = "Equipes actives avec membres et chantier actif (une requete groupee).")
    public ResponseEntity<ApiResponse<List<com.cms.equipe.dto.EquipeDetailResponse>>> listerDetaillees() {
        return ResponseEntity.ok(ApiResponse.success(equipeDetailService.listerDetaillees()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Consulter une equipe", description = "Detail d'une equipe par identifiant.")
    public ResponseEntity<ApiResponse<EquipeResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(equipeService.trouverParId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('EQUIPE','MODIFIER')")
    @Operation(summary = "Modifier une equipe", description = "Met a jour les informations d'une equipe.")
    public ResponseEntity<ApiResponse<EquipeResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Equipe modifiee", equipeService.modifier(id, request)));
    }

    // ------------------------------------------------------------------
    // Membres d'equipe
    // ------------------------------------------------------------------

    @PostMapping("/membres")
    @PreAuthorize("hasPermission('EQUIPE','MODIFIER')")
    @Operation(summary = "Ajouter un membre", description = "Integre un utilisateur dans une equipe (role CHEF ou OUVRIER).")
    public ResponseEntity<ApiResponse<MembreEquipeResponse>> ajouterMembre(
            @Valid @RequestBody CreateMembreEquipeRequest request) {
        MembreEquipeResponse response = membreEquipeService.integrer(
                request.getEquipeId(), request.getUtilisateurId(),
                request.getRoleDansEquipe(), request.getDateIntegration());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Membre ajoute", response));
    }

    @PutMapping("/membres/{membreId}")
    @PreAuthorize("hasPermission('EQUIPE','MODIFIER')")
    @Operation(summary = "Modifier le role d'un membre", description = "Change le role d'un membre (CHEF / OUVRIER).")
    public ResponseEntity<ApiResponse<MembreEquipeResponse>> modifierRoleMembre(
            @PathVariable Long membreId,
            @Valid @RequestBody UpdateMembreEquipeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role modifie",
                membreEquipeService.changerRole(membreId, request.getRoleDansEquipe())));
    }

    @DeleteMapping("/membres/{membreId}")
    @PreAuthorize("hasPermission('EQUIPE','SUPPRIMER')")
    @Operation(summary = "Retirer un membre", description = "Retire un utilisateur d'une equipe.")
    public ResponseEntity<ApiResponse<Void>> retirerMembre(@PathVariable Long membreId) {
        membreEquipeService.retirer(membreId);
        return ResponseEntity.ok(ApiResponse.success("Membre retire", null));
    }

    @GetMapping("/{equipeId}/membres")
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Lister les membres", description = "Liste des membres d'une equipe.")
    public ResponseEntity<ApiResponse<List<MembreEquipeResponse>>> listerMembres(@PathVariable Long equipeId) {
        return ResponseEntity.ok(ApiResponse.success(membreEquipeService.listerMembres(equipeId)));
    }

    // ------------------------------------------------------------------
    // Affectations equipe - chantier
    // ------------------------------------------------------------------

    @PostMapping("/affectations")
    @PreAuthorize("hasPermission('EQUIPE','MODIFIER')")
    @Operation(summary = "Affecter une equipe a un chantier", description = "Cree une affectation equipe - chantier.")
    public ResponseEntity<ApiResponse<AffectationEquipeChantierResponse>> affecterChantier(
            @Valid @RequestBody CreateAffectationEquipeChantierRequest request) {
        AffectationEquipeChantierResponse response = affectationEquipeChantierService.affecter(
                request.getEquipeId(), request.getChantierId(),
                request.getDateDebut(), request.getDateFin());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Equipe affectee au chantier", response));
    }

    @PutMapping("/affectations/{affectationId}/terminer")
    @PreAuthorize("hasPermission('EQUIPE','MODIFIER')")
    @Operation(summary = "Terminer une affectation", description = "Passe une affectation au statut TERMINEE.")
    public ResponseEntity<ApiResponse<AffectationEquipeChantierResponse>> terminerAffectation(
            @PathVariable Long affectationId) {
        return ResponseEntity.ok(ApiResponse.success("Affectation terminee",
                affectationEquipeChantierService.terminer(affectationId)));
    }

    @GetMapping("/{equipeId}/affectations")
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Lister les chantiers d'une equipe", description = "Affectations d'une equipe.")
    public ResponseEntity<ApiResponse<List<AffectationEquipeChantierResponse>>> listerChantiersDeEquipe(
            @PathVariable Long equipeId) {
        return ResponseEntity.ok(ApiResponse.success(affectationEquipeChantierService.listerChantiersDeEquipe(equipeId)));
    }

    @GetMapping("/chantiers/{chantierId}/affectations")
    @PreAuthorize("hasPermission('EQUIPE','LIRE')")
    @Operation(summary = "Lister les equipes d'un chantier", description = "Affectations d'un chantier.")
    public ResponseEntity<ApiResponse<List<AffectationEquipeChantierResumeResponse>>> listerEquipesDuChantier(
            @PathVariable Long chantierId) {
        return ResponseEntity.ok(ApiResponse.success(affectationEquipeChantierService.listerEquipesDuChantier(chantierId)));
    }

}

package com.cms.utilisateur.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.dto.SearchRequest;
import com.cms.common.response.ApiResponse;
import com.cms.common.response.PageResponse;
import com.cms.utilisateur.dto.ModifierProfilRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurPermissionResponse;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;
import com.cms.utilisateur.service.UtilisateurService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module utilisateur.
 *
 * <p>Le Controller verifie uniquement l'autorisation fonctionnelle
 * (RBAC Niveau 1 via {@code @PreAuthorize}). Le perimetre des donnees
 * (Niveau 2) sera controle dans les Services.
 */
@RestController
@RequestMapping(ApiRoutes.USERS)
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    @PreAuthorize("hasPermission('UTILISATEUR','LIRE')")
    @Operation(summary = "Rechercher des utilisateurs", description = "Recherche paginee d'utilisateurs.")
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurResumeResponse>>> rechercher(
            @ModelAttribute SearchRequest search) {
        Page<UtilisateurResumeResponse> page = utilisateurService.rechercher(search);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('UTILISATEUR','LIRE')")
    @Operation(summary = "Consulter un utilisateur", description = "Detail d'un utilisateur par identifiant.")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(utilisateurService.trouverParId(id)));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasPermission('UTILISATEUR','LIRE')")
    @Operation(summary = "Consulter un utilisateur par email", description = "Detail d'un utilisateur par email.")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> trouverParEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(utilisateurService.trouverParEmail(email)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modifier son propre compte", description = "Chaque utilisateur ne peut modifier que son propre compte (nom, prenom, email, telephone).")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUtilisateurRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Utilisateur modifie",
                utilisateurService.modifierUtilisateur(id, request)));
    }

    @PutMapping("/{id}/profil")
    @PreAuthorize("hasPermission('PROFIL','MODIFIER')")
    @Operation(summary = "Changer le profil d'un utilisateur",
            description = "Reattribue le profil (role) d'un utilisateur. Operation administrative : jamais utilisee par l'inscription publique.")
    public ResponseEntity<ApiResponse<UtilisateurResponse>> modifierProfil(
            @PathVariable Long id,
            @Valid @RequestBody ModifierProfilRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profil mis a jour",
                utilisateurService.modifierProfil(id, request.getProfilId())));
    }

    @PostMapping("/{id}/permissions/{permissionId}/accorder")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Accorder une permission individuelle", description = "Exception RBAC ACCORDER pour un utilisateur.")
    public ResponseEntity<ApiResponse<UtilisateurPermissionResponse>> accorderPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success("Permission accordee",
                utilisateurService.accorderPermission(id, permissionId)));
    }

    @PostMapping("/{id}/permissions/{permissionId}/refuser")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Refuser une permission individuelle", description = "Exception RBAC REFUSER (prioritaire) pour un utilisateur.")
    public ResponseEntity<ApiResponse<UtilisateurPermissionResponse>> refuserPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success("Permission refusee",
                utilisateurService.refuserPermission(id, permissionId)));
    }

    @DeleteMapping("/permissions/{exceptionId}")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Retirer une exception individuelle", description = "Supprime une exception ACCORDER ou REFUSER.")
    public ResponseEntity<ApiResponse<Void>> retirerPermissionIndividuelle(@PathVariable Long exceptionId) {
        utilisateurService.retirerPermissionIndividuelle(exceptionId);
        return ResponseEntity.ok(ApiResponse.success("Exception individuelle retiree", null));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasPermission('UTILISATEUR','LIRE')")
    @Operation(summary = "Lister les permissions individuelles", description = "Exceptions RBAC individuelles d'un utilisateur.")
    public ResponseEntity<ApiResponse<java.util.List<UtilisateurPermissionResponse>>> listerPermissions(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(utilisateurService.listerPermissionsIndividuelles(id)));
    }

}

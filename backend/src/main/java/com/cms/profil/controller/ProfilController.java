package com.cms.profil.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilPermissionResponse;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.dto.ProfilResumeResponse;
import com.cms.profil.dto.UpdateProfilRequest;
import com.cms.profil.service.ProfilService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du module profil (roles RBAC).
 */
@RestController
@RequestMapping(ApiRoutes.PROFILS)
@Tag(name = "Profils", description = "Gestion des profils (roles RBAC) et de leurs permissions")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('PROFIL','CREER')")
    @Operation(summary = "Creer un profil", description = "Cree un profil (role RBAC).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profil cree"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Nom de profil deja utilise")
    })
    public ResponseEntity<ApiResponse<ProfilResponse>> creer(@Valid @RequestBody CreateProfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profil cree", profilService.creerProfil(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('PROFIL','LIRE')")
    @Operation(summary = "Rechercher des profils", description = "Recherche paginee de profils.")
    public ResponseEntity<ApiResponse<PageResponse<ProfilResumeResponse>>> rechercher(
            @ModelAttribute SearchRequest search) {
        Page<ProfilResumeResponse> page = profilService.rechercher(search);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('PROFIL','LIRE')")
    @Operation(summary = "Consulter un profil", description = "Detail d'un profil par identifiant.")
    public ResponseEntity<ApiResponse<ProfilResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(profilService.trouverParId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('PROFIL','MODIFIER')")
    @Operation(summary = "Modifier un profil", description = "Met a jour un profil (nom, description).")
    public ResponseEntity<ApiResponse<ProfilResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfilRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profil modifie", profilService.modifierProfil(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('PROFIL','SUPPRIMER')")
    @Operation(summary = "Supprimer un profil", description = "Supprime un profil (interdit pour les profils systeme ou attribues).")
    public ResponseEntity<ApiResponse<Void>> supprimer(@PathVariable Long id) {
        profilService.supprimerProfil(id);
        return ResponseEntity.ok(ApiResponse.success("Profil supprime", null));
    }

    @PostMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasPermission('PROFIL','MODIFIER')")
    @Operation(summary = "Attribuer une permission a un profil", description = "Cree l'association profil / permission.")
    public ResponseEntity<ApiResponse<ProfilPermissionResponse>> ajouterPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success("Permission attribuee",
                profilService.ajouterPermission(id, permissionId)));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasPermission('PROFIL','MODIFIER')")
    @Operation(summary = "Retirer une permission d'un profil", description = "Supprime l'association profil / permission.")
    public ResponseEntity<ApiResponse<Void>> retirerPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        profilService.retirerPermission(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission retiree", null));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasPermission('PROFIL','LIRE')")
    @Operation(summary = "Lister les permissions d'un profil", description = "Associations profil / permission.")
    public ResponseEntity<ApiResponse<List<ProfilPermissionResponse>>> listerPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(profilService.listerPermissions(id)));
    }

}

package com.cms.permission.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.cms.common.constants.ApiRoutes;
import com.cms.common.dto.SearchRequest;
import com.cms.common.response.ApiResponse;
import com.cms.common.response.PageResponse;
import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.dto.PermissionResumeResponse;
import com.cms.permission.dto.UpdatePermissionRequest;
import com.cms.permission.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API REST du referentiel des permissions (RBAC).
 */
@RestController
@RequestMapping(ApiRoutes.PERMISSIONS)
@Tag(name = "Permissions", description = "Gestion du referentiel des permissions RBAC")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('PERMISSION','CREER')")
    @Operation(summary = "Creer une permission", description = "Cree une permission (nom technique unique MODULE_ACTION).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permission creee"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Nom de permission deja utilise")
    })
    public ResponseEntity<ApiResponse<PermissionResponse>> creer(@Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission creee", permissionService.creerPermission(request)));
    }

    @GetMapping
    @PreAuthorize("hasPermission('PERMISSION','LIRE')")
    @Operation(summary = "Rechercher des permissions", description = "Recherche paginee de permissions.")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResumeResponse>>> rechercher(
            @ModelAttribute SearchRequest search) {
        Page<PermissionResumeResponse> page = permissionService.rechercher(search);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION','LIRE')")
    @Operation(summary = "Consulter une permission", description = "Detail d'une permission par identifiant.")
    public ResponseEntity<ApiResponse<PermissionResponse>> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.trouverParId(id)));
    }

    @GetMapping("/nom/{nomPermission}")
    @PreAuthorize("hasPermission('PERMISSION','LIRE')")
    @Operation(summary = "Consulter une permission par nom", description = "Detail d'une permission par nom technique (ex : TACHE_LIRE).")
    public ResponseEntity<ApiResponse<PermissionResponse>> trouverParNomPermission(@PathVariable String nomPermission) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.trouverParNomPermission(nomPermission)));
    }

    @GetMapping("/modules/{module}")
    @PreAuthorize("hasPermission('PERMISSION','LIRE')")
    @Operation(summary = "Lister les permissions d'un module", description = "Permissions d'un module (ex : TACHE).")
    public ResponseEntity<ApiResponse<List<PermissionResumeResponse>>> listerParModule(@PathVariable String module) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.listerParModule(module)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Modifier une permission", description = "Met a jour nom / nomPermission / module / description.")
    public ResponseEntity<ApiResponse<PermissionResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Permission modifiee",
                permissionService.modifierPermission(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('PERMISSION','SUPPRIMER')")
    @Operation(summary = "Supprimer une permission", description = "Supprime definitivement la permission et ses octrois.")
    public ResponseEntity<ApiResponse<Void>> supprimer(@PathVariable Long id) {
        permissionService.supprimerPermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission supprimee", null));
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Desactiver une permission", description = "Retire les octrois sans supprimer le referentiel.")
    public ResponseEntity<ApiResponse<PermissionResponse>> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Permission desactivee", permissionService.desactiverPermission(id)));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Activer une permission", description = "Rend la permission a nouveau attribuable (referentiel permanent).")
    public ResponseEntity<ApiResponse<PermissionResponse>> activer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Permission activee", permissionService.activerPermission(id)));
    }

}

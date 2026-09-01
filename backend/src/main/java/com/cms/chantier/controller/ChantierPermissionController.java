package com.cms.chantier.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cms.chantier.dto.UtilisateurPermissionChantierResponse;
import com.cms.chantier.service.UtilisateurPermissionChantierService;
import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API REST des permissions scopées par chantier.
 *
 * <p>Permet d'accorder une permission à un utilisateur POUR UN CHANTIER
 * PRÉCIS (« chef du chantier A, pas du chantier B »). Les exceptions sont
 * stockées dans {@code utilisateur_permission_chantier} (V11).
 */
@RestController
@RequestMapping(ApiRoutes.CHANTIERS + "/{chantierId}/permissions")
@Tag(name = "Permissions par chantier", description = "Exceptions RBAC scopées à un chantier")
public class ChantierPermissionController {

    private final UtilisateurPermissionChantierService service;

    public ChantierPermissionController(UtilisateurPermissionChantierService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasPermission('PERMISSION','LIRE')")
    @Operation(summary = "Lister les permissions scopées d'un chantier",
            description = "Exceptions ACCORDER/REFUSER accordées aux utilisateurs pour CE chantier uniquement.")
    public ResponseEntity<ApiResponse<List<UtilisateurPermissionChantierResponse>>> lister(
            @PathVariable Long chantierId) {
        return ResponseEntity.ok(ApiResponse.success(service.listerParChantier(chantierId)));
    }

    @PostMapping("/{utilisateurId}/{permissionId}/accorder")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Accorder une permission sur ce chantier",
            description = "Exception ACCORDER scopée : l'utilisateur a la permission uniquement sur CE chantier.")
    public ResponseEntity<ApiResponse<UtilisateurPermissionChantierResponse>> accorder(
            @PathVariable Long chantierId,
            @PathVariable Long utilisateurId,
            @PathVariable Long permissionId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission accordee sur le chantier",
                        service.accorder(chantierId, utilisateurId, permissionId)));
    }

    @PostMapping("/{utilisateurId}/{permissionId}/refuser")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Refuser une permission sur ce chantier",
            description = "Exception REFUSER scopée (prioritaire sur le profil) : retranchée sur CE chantier uniquement.")
    public ResponseEntity<ApiResponse<UtilisateurPermissionChantierResponse>> refuser(
            @PathVariable Long chantierId,
            @PathVariable Long utilisateurId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success("Permission refusee sur le chantier",
                service.refuser(chantierId, utilisateurId, permissionId)));
    }

    @DeleteMapping("/{exceptionId}")
    @PreAuthorize("hasPermission('PERMISSION','MODIFIER')")
    @Operation(summary = "Retirer une exception scopée",
            description = "Supprime une exception ACCORDER ou REFUSER scopée à un chantier.")
    public ResponseEntity<ApiResponse<Void>> retirer(@PathVariable Long chantierId,
                                                     @PathVariable Long exceptionId) {
        service.retirer(exceptionId);
        return ResponseEntity.ok(ApiResponse.success("Exception scopee retiree", null));
    }

}
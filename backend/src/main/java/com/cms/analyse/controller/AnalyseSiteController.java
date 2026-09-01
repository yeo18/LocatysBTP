package com.cms.analyse.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cms.analyse.dto.AnalyseSiteResponse;
import com.cms.analyse.dto.GeocodageResultatResponse;
import com.cms.analyse.service.AnalyseSiteService;
import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API REST du module « Analyse du site ».
 *
 * <p>Le géocodage est une action explicite (jamais en autocomplétion).
 * L'analyse d'un chantier exige une position confirmée et l'accès RBAC au
 * chantier (Niveau 1 via @PreAuthorize, Niveau 2 via DataAccessService).
 */
@RestController
@RequestMapping(ApiRoutes.ANALYSE_SITE)
@Tag(name = "Analyse du site", description = "Géocodage, météo, environnement et synthèse d'un chantier")
public class AnalyseSiteController {

    private final AnalyseSiteService analyseSiteService;

    public AnalyseSiteController(AnalyseSiteService analyseSiteService) {
        this.analyseSiteService = analyseSiteService;
    }

    @GetMapping("/geocoder")
    @PreAuthorize("hasPermission('CHANTIER','LIRE')")
    @Operation(summary = "Localiser un lieu", description = "Recherche Nominatim d'une position a partir d'une description libre. Action explicite (pas d'autocompletion).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Positions proposees"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Description manquante"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifie"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission CHANTIER_LIRE requise")
    })
    public ResponseEntity<ApiResponse<List<GeocodageResultatResponse>>> geocoder(
            @RequestParam("q") String description) {
        return ResponseEntity.ok(ApiResponse.success(analyseSiteService.geocoder(description)));
    }

    @GetMapping("/chantiers/{id}/analyser")
    @PreAuthorize("hasPermission(#id, 'CHANTIER', 'LIRE')")
    @Operation(summary = "Analyser un chantier", description = "Lance l'analyse complete (meteo, environnement, synthese) depuis la position confirmee du chantier.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analyse terminee"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Position non confirmee"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acces au chantier refuse")
    })
    public ResponseEntity<ApiResponse<AnalyseSiteResponse>> analyser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(analyseSiteService.analyser(id)));
    }

}

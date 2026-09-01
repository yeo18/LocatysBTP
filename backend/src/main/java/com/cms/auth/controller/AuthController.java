package com.cms.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cms.auth.dto.ChangerMotDePasseRequest;
import com.cms.auth.dto.LoginRequest;
import com.cms.auth.dto.MeResponse;
import com.cms.auth.dto.TokenResponse;
import com.cms.auth.service.AuthenticationService;
import com.cms.common.constants.ApiRoutes;
import com.cms.common.response.ApiResponse;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints publics d'authentification (inscription et connexion).
 *
 * <p>Routes publiques : {@code /api/v1/auth/register} et
 * {@code /api/v1/auth/login} (permitAll dans {@code SecurityConfig}).
 * {@code /auth/me} est protege (authentification requise).
 */
@RestController
@RequestMapping(ApiRoutes.AUTH)
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs (routes publiques)")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Cree un compte utilisateur (route publique).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Compte cree"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email deja utilise")
    })
    public ResponseEntity<ApiResponse<UtilisateurResponse>> inscrire(
            @Valid @RequestBody CreateUtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription reussie", authenticationService.inscrire(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et renvoie un token JWT (route publique).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connexion reussie (token JWT)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    })
    public ResponseEntity<ApiResponse<TokenResponse>> connexion(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Connexion reussie", authenticationService.connexion(request)));
    }

    @GetMapping("/me")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil courant")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Profil courant", authenticationService.me()));
    }

    @PostMapping("/changer-mot-de-passe")
    @Operation(summary = "Changer son mot de passe", description = "Modifie le mot de passe de l'utilisateur authentifie (ancien mot de passe requis).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe change"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect ou confirmation non conforme"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifie")
    })
    public ResponseEntity<ApiResponse<Void>> changerMotDePasse(@Valid @RequestBody ChangerMotDePasseRequest request) {
        authenticationService.changerMotDePasse(request);
        return ResponseEntity.ok(ApiResponse.success("Mot de passe change avec succes", null));
    }

}

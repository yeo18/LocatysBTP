package com.cms.tache.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.common.constants.ApiRoutes;
import com.cms.exception.custom.ForbiddenException;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.ValidationTacheResponse;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.enums.ValidationTacheStatut;
import com.cms.tache.service.AffectationTacheService;
import com.cms.tache.service.TacheService;
import com.cms.tache.service.ValidationTacheService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Tache (LOOP 3.15).
 *
 * <p>Les 5 cas obligatoires : creation par un ADMINISTRATEUR (201), refus
 * sans permission (403), consultation par un utilisateur affecte (200),
 * refus pour un utilisateur hors perimetre (403), historique de validation
 * conserve.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TacheControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TacheService tacheService;
    @MockBean
    private AffectationTacheService affectationTacheService;
    @MockBean
    private ValidationTacheService validationTacheService;
    @MockBean
    private DroitsService droitsService;

    private CreateTacheRequest creerRequest() {
        CreateTacheRequest request = new CreateTacheRequest();
        request.setTitre("Fondations");
        request.setDescription("Coulage des fondations");
        request.setPriorite(Priorite.HAUTE);
        request.setDateDebut(LocalDate.of(2026, 8, 10));
        request.setDateFin(LocalDate.of(2026, 8, 20));
        request.setChantierId(1L);
        return request;
    }

    // ------------------------------------------------------------------
    // Cas 1 : l'ADMINISTRATEUR cree une tache -> succes (201)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationTacheAutorisee_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_CREER"));

        TacheResponse response = new TacheResponse();
        response.setId(1L);
        response.setTitre("Fondations");
        response.setChantierId(1L);
        when(tacheService.createTache(any(CreateTacheRequest.class))).thenReturn(response);

        mockMvc.perform(post(ApiRoutes.TACHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.titre").value("Fondations"));
    }

    // ------------------------------------------------------------------
    // Cas 2 : utilisateur sans permission cree une tache -> 403
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationTacheRefusee_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of());

        mockMvc.perform(post(ApiRoutes.TACHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Cas 3 : l'utilisateur affecte consulte la tache -> succes (200)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void consultationTacheAffecte_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_LIRE"));

        TacheResponse response = new TacheResponse();
        response.setId(1L);
        response.setTitre("Fondations");
        response.setChantierId(1L);
        when(tacheService.findById(1L)).thenReturn(response);

        mockMvc.perform(get(ApiRoutes.TACHES + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titre").value("Fondations"));
    }

    // ------------------------------------------------------------------
    // Cas 4 : utilisateur hors equipe tente d'acceder -> 403
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void consultationTacheHorsPerimetre_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_LIRE"));
        when(tacheService.findById(1L)).thenThrow(ForbiddenException.generic());

        mockMvc.perform(get(ApiRoutes.TACHES + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Cas 5 : validation d'une tache -> historique conserve
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void validationTache_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_VALIDER"));

        ValidationTacheResponse response = new ValidationTacheResponse();
        response.setId(1L);
        response.setTacheId(1L);
        response.setStatut(ValidationTacheStatut.VALIDE);
        response.setDateValidation(LocalDate.of(2026, 8, 25));
        when(validationTacheService.validateTache(any(CreateValidationTacheRequest.class))).thenReturn(response);

        CreateValidationTacheRequest request = new CreateValidationTacheRequest();
        request.setTacheId(1L);
        request.setStatut(ValidationTacheStatut.VALIDE);
        request.setCommentaire("Conforme");
        request.setDateValidation(LocalDate.of(2026, 8, 25));

        mockMvc.perform(post(ApiRoutes.TACHES + "/1/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.statut").value("VALIDE"));
    }

    @Test
    @WithUtilisateur
    void refusTache_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_REFUSER"));

        ValidationTacheResponse response = new ValidationTacheResponse();
        response.setId(2L);
        response.setTacheId(1L);
        response.setStatut(ValidationTacheStatut.REFUSE);
        response.setDateValidation(LocalDate.of(2026, 8, 25));
        when(validationTacheService.validateTache(any(CreateValidationTacheRequest.class))).thenReturn(response);

        CreateValidationTacheRequest request = new CreateValidationTacheRequest();
        request.setTacheId(1L);
        request.setStatut(ValidationTacheStatut.REFUSE);
        request.setCommentaire("Non conforme sur le terrain");
        request.setDateValidation(LocalDate.of(2026, 8, 25));

        mockMvc.perform(post(ApiRoutes.TACHES + "/1/refuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.statut").value("REFUSE"));
    }

    @Test
    @WithUtilisateur
    void historiqueValidation_retourneListe() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("TACHE_LIRE"));

        ValidationTacheResponse v1 = new ValidationTacheResponse();
        v1.setId(1L);
        v1.setTacheId(1L);
        v1.setStatut(ValidationTacheStatut.VALIDE);
        v1.setDateValidation(LocalDate.of(2026, 8, 25));

        ValidationTacheResponse v2 = new ValidationTacheResponse();
        v2.setId(2L);
        v2.setTacheId(1L);
        v2.setStatut(ValidationTacheStatut.REFUSE);
        v2.setDateValidation(LocalDate.of(2026, 8, 26));

        when(validationTacheService.getValidationHistory(1L)).thenReturn(List.of(v1, v2));

        mockMvc.perform(get(ApiRoutes.TACHES + "/1/validations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ------------------------------------------------------------------
    // Securite de base : sans token -> 401
    // ------------------------------------------------------------------

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.TACHES))
                .andExpect(status().isUnauthorized());
    }

}

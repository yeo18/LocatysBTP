package com.cms.template.controller;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.cms.common.constants.ApiRoutes;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.service.TemplateTacheService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller TemplateTache (LOOP 5.10).
 *
 * <p>Verifie notamment l'ABSENCE de tout endpoint de desactivation
 * (le modele TemplateTache ne possede pas de statut).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TemplateTacheControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateTacheService templateTacheService;
    @MockBean
    private DroitsService droitsService;

    private void autoriser(String... droits) {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of(droits));
    }

    private CreateTemplateTacheRequest creerRequest() {
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre("Dalle beton");
        request.setPriorite(Priorite.HAUTE);
        return request;
    }

    private TemplateTacheResponse response(Long id) {
        TemplateTacheResponse response = new TemplateTacheResponse();
        response.setId(id);
        response.setTitre("Dalle beton");
        response.setPriorite(Priorite.HAUTE);
        return response;
    }

    // ------------------------------------------------------------------
    // 5. creation
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationTemplateTache_retourne201() throws Exception {
        autoriser("TEMPLATE_TACHE_CREER");
        when(templateTacheService.creer(any(CreateTemplateTacheRequest.class)))
                .thenReturn(response(1L));

        mockMvc.perform(post(ApiRoutes.TEMPLATE_TACHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.titre").value("Dalle beton"));
    }

    // ------------------------------------------------------------------
    // 6. lecture
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void consultationTemplateTache_retourne200() throws Exception {
        autoriser("TEMPLATE_TACHE_LIRE");
        when(templateTacheService.trouverParId(1L)).thenReturn(response(1L));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_TACHES + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titre").value("Dalle beton"));
    }

    @Test
    @WithUtilisateur
    void listeTemplateTache_retourne200() throws Exception {
        autoriser("TEMPLATE_TACHE_LIRE");
        when(templateTacheService.lister()).thenReturn(List.of(response(1L)));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_TACHES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ------------------------------------------------------------------
    // 7. modification
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void modificationTemplateTache_retourne200() throws Exception {
        autoriser("TEMPLATE_TACHE_MODIFIER");
        when(templateTacheService.modifier(anyLong(), any(UpdateTemplateTacheRequest.class)))
                .thenReturn(response(1L));

        UpdateTemplateTacheRequest request = new UpdateTemplateTacheRequest();
        request.setTitre("Dalle beton armee");
        request.setPriorite(Priorite.MOYENNE);

        mockMvc.perform(put(ApiRoutes.TEMPLATE_TACHES + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    // ------------------------------------------------------------------
    // 9. import TemplateTache -> Chantier
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void importTemplateTache_retourne201() throws Exception {
        autoriser("TEMPLATE_TACHE_LIRE");
        when(templateTacheService.importerDansChantier(1L, 5L)).thenReturn(1);

        mockMvc.perform(post(ApiRoutes.TEMPLATE_TACHES + "/1/import")
                        .param("chantierId", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(1));
    }

    // ------------------------------------------------------------------
    // 10-12. validations et erreurs
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationRequestInvalide_retourne400() throws Exception {
        autoriser("TEMPLATE_TACHE_CREER");
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre("");

        mockMvc.perform(post(ApiRoutes.TEMPLATE_TACHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUtilisateur
    void ressourceInexistante_retourne404() throws Exception {
        autoriser("TEMPLATE_TACHE_LIRE");
        when(templateTacheService.trouverParId(999L))
                .thenThrow(new ResourceNotFoundException("TemplateTache introuvable avec l'identifiant : 999"));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_TACHES + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // RBAC (LOOP 5.11)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationSansPermission_retourne403() throws Exception {
        autoriser();

        mockMvc.perform(post(ApiRoutes.TEMPLATE_TACHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithUtilisateur
    void lectureSansPermission_retourne403() throws Exception {
        autoriser();

        mockMvc.perform(get(ApiRoutes.TEMPLATE_TACHES + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUtilisateur
    void modificationSansPermission_retourne403() throws Exception {
        autoriser();

        UpdateTemplateTacheRequest request = new UpdateTemplateTacheRequest();
        request.setTitre("Dalle beton armee");
        request.setPriorite(Priorite.MOYENNE);

        mockMvc.perform(put(ApiRoutes.TEMPLATE_TACHES + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // securite de base : sans token -> 401
    // ------------------------------------------------------------------

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.TEMPLATE_TACHES))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // 13. aucun endpoint de desactivation sur TemplateTache
    // ------------------------------------------------------------------

    @Test
    void aucunEndpointDesactivationSurTemplateTache() throws Exception {
        Method[] methodes = TemplateTacheController.class.getDeclaredMethods();
        for (Method methode : methodes) {
            assertThat(methode.isAnnotationPresent(PatchMapping.class)).isFalse();
            assertThat(methode.isAnnotationPresent(DeleteMapping.class)).isFalse();
        }
    }

}

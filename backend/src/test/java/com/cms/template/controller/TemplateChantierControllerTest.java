package com.cms.template.controller;

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
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.service.TemplateChantierService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller TemplateChantier (LOOP 5.10).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TemplateChantierControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateChantierService templateChantierService;
    @MockBean
    private DroitsService droitsService;

    private void autoriser(String... droits) {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of(droits));
    }

    private CreateTemplateChantierRequest creerRequest() {
        CreateTemplateChantierRequest request = new CreateTemplateChantierRequest();
        request.setNom("Maison R+1");
        request.setTypeConstruction(TypeConstruction.MAISON_R1);
        return request;
    }

    private TemplateChantierResponse response(Long id) {
        TemplateChantierResponse response = new TemplateChantierResponse();
        response.setId(id);
        response.setNom("Maison R+1");
        response.setTypeConstruction(TypeConstruction.MAISON_R1);
        response.setStatut(TemplateChantierStatut.ACTIF);
        return response;
    }

    // ------------------------------------------------------------------
    // 1. creation
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationTemplateChantier_retourne201() throws Exception {
        autoriser("TEMPLATE_CHANTIER_CREER");
        when(templateChantierService.creer(any(CreateTemplateChantierRequest.class)))
                .thenReturn(response(1L));

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.statut").value("ACTIF"));
    }

    // ------------------------------------------------------------------
    // 2. lecture
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void consultationTemplateChantier_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.trouverParId(1L)).thenReturn(response(1L));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nom").value("Maison R+1"));
    }

    @Test
    @WithUtilisateur
    void listeTemplateChantier_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.lister()).thenReturn(List.of(response(1L)));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ------------------------------------------------------------------
    // 3. modification
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void modificationTemplateChantier_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_MODIFIER");
        when(templateChantierService.modifier(anyLong(), any(UpdateTemplateChantierRequest.class)))
                .thenReturn(response(1L));

        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Maison R+1");
        request.setTypeConstruction(TypeConstruction.MAISON_R1);

        mockMvc.perform(put(ApiRoutes.TEMPLATE_CHANTIERS + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    // ------------------------------------------------------------------
    // 4. desactivation (PATCH prevue par le modele)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void desactivationTemplateChantier_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_MODIFIER");
        TemplateChantierResponse inactive = response(1L);
        inactive.setStatut(TemplateChantierStatut.INACTIF);
        when(templateChantierService.desactiver(1L)).thenReturn(inactive);

        mockMvc.perform(patch(ApiRoutes.TEMPLATE_CHANTIERS + "/1/desactiver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statut").value("INACTIF"));
    }

    // ------------------------------------------------------------------
    // association TemplateChantier <-> TemplateTache
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void associationTemplateTache_retourne201() throws Exception {
        autoriser("TEMPLATE_CHANTIER_MODIFIER");
        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS + "/1/taches/2"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUtilisateur
    void retraitTemplateTache_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_MODIFIER");
        mockMvc.perform(delete(ApiRoutes.TEMPLATE_CHANTIERS + "/1/taches/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUtilisateur
    void consultationTachesAssociees_retourne200() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        TemplateTacheResumeResponse resume = new TemplateTacheResumeResponse();
        resume.setId(2L);
        resume.setTitre("Dalle beton");
        resume.setPriorite(Priorite.HAUTE);
        when(templateChantierService.trouverTachesAssociees(1L)).thenReturn(List.of(resume));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS + "/1/taches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].titre").value("Dalle beton"));
    }

    // ------------------------------------------------------------------
    // 8. import TemplateChantier -> Chantier
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void importTemplateChantier_retourne201() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.importerDansChantier(1L, 5L)).thenReturn(3);

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS + "/1/import")
                        .param("chantierId", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(3));
    }

    // ------------------------------------------------------------------
    // 10-12. validations et erreurs
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationRequestInvalide_retourne400() throws Exception {
        autoriser("TEMPLATE_CHANTIER_CREER");
        CreateTemplateChantierRequest request = new CreateTemplateChantierRequest();
        request.setNom("");
        request.setTypeConstruction(null);

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUtilisateur
    void ressourceInexistante_retourne404() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.trouverParId(999L))
                .thenThrow(new ResourceNotFoundException("TemplateChantier introuvable avec l'identifiant : 999"));

        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithUtilisateur
    void importChantierDesactive_retourne400() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.importerDansChantier(1L, 5L))
                .thenThrow(new BadRequestException("Impossible d'importer un TemplateChantier desactive"));

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS + "/1/import")
                        .param("chantierId", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // RBAC (LOOP 5.11)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationSansPermission_retourne403() throws Exception {
        autoriser();

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithUtilisateur
    void lectureSansPermission_retourne403() throws Exception {
        autoriser();

        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUtilisateur
    void modificationSansPermission_retourne403() throws Exception {
        autoriser();

        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Villa");
        request.setTypeConstruction(TypeConstruction.VILLA);

        mockMvc.perform(put(ApiRoutes.TEMPLATE_CHANTIERS + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUtilisateur
    void importChantierHorsPerimetre_retourne403() throws Exception {
        autoriser("TEMPLATE_CHANTIER_LIRE");
        when(templateChantierService.importerDansChantier(1L, 5L))
                .thenThrow(ForbiddenException.generic());

        mockMvc.perform(post(ApiRoutes.TEMPLATE_CHANTIERS + "/1/import")
                        .param("chantierId", "5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // securite de base : sans token -> 401
    // ------------------------------------------------------------------

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.TEMPLATE_CHANTIERS))
                .andExpect(status().isUnauthorized());
    }

}

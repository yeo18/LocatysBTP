package com.cms.permission.controller;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.common.constants.ApiRoutes;
import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.service.PermissionService;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Permission (RBAC).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;
    @MockBean
    private DroitsService droitsService;

    @Test
    @WithUtilisateur
    void creationAutorisee_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("PERMISSION_CREER"));
        PermissionResponse response = new PermissionResponse();
        response.setId(1L);
        response.setNomPermission("TACHE_LIRE");
        when(permissionService.creerPermission(any(CreatePermissionRequest.class))).thenReturn(response);

        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setNom("Lire les taches");
        request.setNomPermission("TACHE_LIRE");
        request.setModule("TACHE");

        mockMvc.perform(post(ApiRoutes.PERMISSIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUtilisateur
    void creationRefusee_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of());

        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setNom("Lire les taches");
        request.setNomPermission("TACHE_LIRE");
        request.setModule("TACHE");

        mockMvc.perform(post(ApiRoutes.PERMISSIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

}

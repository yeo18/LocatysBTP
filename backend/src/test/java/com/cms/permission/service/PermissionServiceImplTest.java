package com.cms.permission.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.exception.custom.NomPermissionDejaExistantException;
import com.cms.exception.custom.PermissionIntrouvableException;
import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.entity.Permission;
import com.cms.permission.mapper.PermissionMapperImpl;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service du referentiel des permissions (OBJECTIF 1).
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    private static final String CODE = "TACHE_CREER";

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ProfilPermissionRepository profilPermissionRepository;
    @Mock
    private UtilisateurPermissionRepository utilisateurPermissionRepository;

    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionServiceImpl(permissionRepository, profilPermissionRepository,
                utilisateurPermissionRepository, new PermissionMapperImpl());
    }

    private CreatePermissionRequest creerRequest() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setNom("Creer une tache");
        request.setNomPermission(CODE);
        request.setModule("tache");
        return request;
    }

    private Permission permissionExistant() {
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setNom("Creer une tache");
        permission.setNomPermission(CODE);
        permission.setModule("TACHE");
        return permission;
    }

    @Test
    void creerPermission_normaliseModule() {
        when(permissionRepository.existsByNomPermission(CODE)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PermissionResponse response = service.creerPermission(creerRequest());

        assertThat(response.getNomPermission()).isEqualTo(CODE);
        assertThat(response.getModule()).isEqualTo("TACHE");
    }

    @Test
    void creerPermission_codeDejaExistant_refuse() {
        when(permissionRepository.existsByNomPermission(CODE)).thenReturn(true);

        assertThatThrownBy(() -> service.creerPermission(creerRequest()))
                .isInstanceOf(NomPermissionDejaExistantException.class);

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void modifierPermission_metAJourModule() {
        Permission existant = permissionExistant();
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(permissionRepository.save(any(Permission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.cms.permission.dto.UpdatePermissionRequest request = new com.cms.permission.dto.UpdatePermissionRequest();
        request.setNom("Supprimer une tache");
        request.setNomPermission("TACHE_SUPPRIMER");
        request.setModule("TACHE");

        PermissionResponse response = service.modifierPermission(1L, request);

        assertThat(response.getModule()).isEqualTo("TACHE");
        assertThat(response.getNomPermission()).isEqualTo("TACHE_SUPPRIMER");
    }

    @Test
    void modifierPermission_inexistante_leveException() {
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        com.cms.permission.dto.UpdatePermissionRequest request = new com.cms.permission.dto.UpdatePermissionRequest();
        request.setNom("Supprimer une tache");
        request.setNomPermission("TACHE_SUPPRIMER");
        request.setModule("TACHE");

        assertThatThrownBy(() -> service.modifierPermission(999L, request))
                .isInstanceOf(PermissionIntrouvableException.class);
    }

    @Test
    void supprimerPermission_nettoyeLesOctroisPuisSupprime() {
        Permission existant = permissionExistant();
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(profilPermissionRepository.findByPermissionId(1L)).thenReturn(List.of());
        when(utilisateurPermissionRepository.findByPermissionId(1L)).thenReturn(List.of());

        service.supprimerPermission(1L);

        verify(permissionRepository).delete(existant);
    }

    @Test
    void desactiverPermission_conserveLeReferentielEtRetireLesOctrois() {
        Permission existant = permissionExistant();
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(profilPermissionRepository.findByPermissionId(1L)).thenReturn(List.of());
        when(utilisateurPermissionRepository.findByPermissionId(1L)).thenReturn(List.of());

        PermissionResponse response = service.desactiverPermission(1L);

        assertThat(response.getNomPermission()).isEqualTo(CODE);
        verify(permissionRepository, never()).delete(existant);
    }

    @Test
    void activerPermission_conserveLeReferentielSansSupprimer() {
        Permission existant = permissionExistant();
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(existant));

        PermissionResponse response = service.activerPermission(1L);

        assertThat(response.getNomPermission()).isEqualTo(CODE);
        verify(permissionRepository, never()).delete(existant);
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    void activerPermission_inexistante_leveException() {
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activerPermission(999L))
                .isInstanceOf(PermissionIntrouvableException.class);
    }

    @Test
    void trouverParNomPermission_inexistant_leveException() {
        when(permissionRepository.findByNomPermission("INEXISTANT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParNomPermission("INEXISTANT"))
                .isInstanceOf(PermissionIntrouvableException.class);
    }

    @Test
    void listerParModule_retournePermissionsDuModule() {
        when(permissionRepository.findByModule("TACHE")).thenReturn(List.of(permissionExistant()));

        var resultat = service.listerParModule("TACHE");

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getNomPermission()).isEqualTo(CODE);
    }

}

package com.cms.profil.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.exception.custom.NomProfilDejaExistantException;
import com.cms.exception.custom.PermissionDejaAttribueeException;
import com.cms.exception.custom.ProfilIntrouvableException;
import com.cms.exception.custom.ProfilSystemeProtegeException;
import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.dto.UpdateProfilRequest;
import com.cms.profil.entity.Profil;
import com.cms.profil.entity.ProfilPermission;
import com.cms.profil.mapper.ProfilMapperImpl;
import com.cms.profil.mapper.ProfilPermissionMapperImpl;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.profil.repository.ProfilRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service des profils (OBJECTIF 2).
 */
@ExtendWith(MockitoExtension.class)
class ProfilServiceImplTest {

    @Mock
    private ProfilRepository profilRepository;
    @Mock
    private ProfilPermissionRepository profilPermissionRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private ProfilServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfilServiceImpl(profilRepository, profilPermissionRepository,
                permissionRepository, new ProfilMapperImpl(), new ProfilPermissionMapperImpl());
    }

    private CreateProfilRequest creerRequest() {
        CreateProfilRequest request = new CreateProfilRequest();
        request.setNom("RESPONSABLE_CHANTIER");
        request.setDescription("Responsable de chantier");
        return request;
    }

    private Profil profilStandard() {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("UTILISATEUR_STANDARD");
        profil.setDescription("Profil par defaut");
        profil.setDateCreation(LocalDateTime.now());
        profil.setDateModification(LocalDateTime.now());
        return profil;
    }

    private Profil profilSysteme() {
        Profil profil = new Profil();
        profil.setId(2L);
        profil.setNom("ADMINISTRATEUR");
        return profil;
    }

    @Test
    void creerProfil_fonctionne() {
        when(profilRepository.existsByNom("RESPONSABLE_CHANTIER")).thenReturn(false);
        when(profilRepository.save(any(Profil.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfilResponse response = service.creerProfil(creerRequest());

        assertThat(response.getNom()).isEqualTo("RESPONSABLE_CHANTIER");
        assertThat(response.getDateCreation()).isNotNull();
    }

    @Test
    void creerProfil_nomDejaExistant_refuse() {
        when(profilRepository.existsByNom("RESPONSABLE_CHANTIER")).thenReturn(true);

        assertThatThrownBy(() -> service.creerProfil(creerRequest()))
                .isInstanceOf(NomProfilDejaExistantException.class);

        verify(profilRepository, never()).save(any(Profil.class));
    }

    @Test
    void supprimerProfil_profilSysteme_interdit() {
        when(profilRepository.findById(2L)).thenReturn(Optional.of(profilSysteme()));

        assertThatThrownBy(() -> service.supprimerProfil(2L))
                .isInstanceOf(ProfilSystemeProtegeException.class);
    }

    @Test
    void supprimerProfil_profilAttribueADesUtilisateurs_interdit() {
        Profil profil = profilStandard();
        profil.setUtilisateurs(new ArrayList<>(List.of(new com.cms.utilisateur.entity.Utilisateur())));
        when(profilRepository.findById(1L)).thenReturn(Optional.of(profil));

        assertThatThrownBy(() -> service.supprimerProfil(1L))
                .isInstanceOf(com.cms.exception.custom.CmsException.class);
    }

    @Test
    void modifierProfil_profilSysteme_renommageInterdit() {
        Profil profil = profilSysteme();
        when(profilRepository.findById(2L)).thenReturn(Optional.of(profil));

        UpdateProfilRequest request = new UpdateProfilRequest();
        request.setNom("SUPER_ADMIN");
        request.setDescription("Renommage interdit");

        assertThatThrownBy(() -> service.modifierProfil(2L, request))
                .isInstanceOf(ProfilSystemeProtegeException.class);
    }

    @Test
    void modifierProfil_profilOrdinaire_fonctionne() {
        Profil profil = profilStandard();
        when(profilRepository.findById(1L)).thenReturn(Optional.of(profil));
        when(profilRepository.save(any(Profil.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfilRequest request = new UpdateProfilRequest();
        request.setNom("UTILISATEUR_STANDARD");
        request.setDescription("Nouvelle description");

        ProfilResponse response = service.modifierProfil(1L, request);

        assertThat(response.getDescription()).isEqualTo("Nouvelle description");
    }

    @Test
    void ajouterPermission_creeAssociation() {
        Profil profil = profilStandard();
        Permission permission = new Permission();
        permission.setId(5L);
        permission.setNomPermission("TACHE_LIRE");

        when(profilRepository.findById(1L)).thenReturn(Optional.of(profil));
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(profilPermissionRepository.findByProfilIdAndPermissionId(1L, 5L)).thenReturn(Optional.empty());
        when(profilPermissionRepository.save(any(ProfilPermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.ajouterPermission(1L, 5L);

        assertThat(response.getProfilId()).isEqualTo(1L);
        assertThat(response.getNomPermission()).isEqualTo("TACHE_LIRE");
    }

    @Test
    void ajouterPermission_dejaAttribuee_refuse() {
        when(profilRepository.findById(1L)).thenReturn(Optional.of(profilStandard()));
        Permission permission = new Permission();
        permission.setId(5L);
        permission.setNomPermission("TACHE_LIRE");
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(profilPermissionRepository.findByProfilIdAndPermissionId(1L, 5L))
                .thenReturn(Optional.of(new ProfilPermission()));

        assertThatThrownBy(() -> service.ajouterPermission(1L, 5L))
                .isInstanceOf(PermissionDejaAttribueeException.class);
    }

    @Test
    void retirerPermission_absente_leveException() {
        when(profilPermissionRepository.findByProfilIdAndPermissionId(1L, 5L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retirerPermission(1L, 5L))
                .isInstanceOf(com.cms.exception.custom.CmsException.class);
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(profilRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(99L))
                .isInstanceOf(ProfilIntrouvableException.class);
    }

}

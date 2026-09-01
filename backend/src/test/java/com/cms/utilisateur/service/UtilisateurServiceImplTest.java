package com.cms.utilisateur.service;

import java.util.Optional;

import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.EmailDejaUtiliseException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.UtilisateurNonTrouveException;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.mapper.UtilisateurMapperImpl;
import com.cms.utilisateur.mapper.UtilisateurPermissionMapper;
import com.cms.utilisateur.mapper.UtilisateurPermissionMapperImpl;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service utilisateur (repositories mockes, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    private static final String EMAIL = "jean.dupont@example.com";
    private static final String MOT_DE_PASSE = "secret123";

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ProfilRepository profilRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UtilisateurPermissionRepository utilisateurPermissionRepository;
    @Mock
    private CurrentUserService currentUserService;

    private UtilisateurMapper utilisateurMapper = new UtilisateurMapperImpl();
    private UtilisateurPermissionMapper utilisateurPermissionMapper = new UtilisateurPermissionMapperImpl();
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UtilisateurServiceImpl service;

    private Profil profilStandard;

    @BeforeEach
    void setUp() {
        service = new UtilisateurServiceImpl(
                utilisateurRepository, profilRepository, utilisateurMapper, passwordEncoder,
                permissionRepository, utilisateurPermissionRepository, utilisateurPermissionMapper,
                currentUserService);
        profilStandard = new Profil();
        profilStandard.setId(1L);
        profilStandard.setNom("UTILISATEUR_STANDARD");
    }

    private CreateUtilisateurRequest creerRequest() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean");
        request.setEmail(EMAIL);
        request.setPassword(MOT_DE_PASSE);
        request.setProfilId(1L);
        return request;
    }

    @Test
    void creerUtilisateur_fonctionne_avecProfilEtPasswordEncode() {
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(profilRepository.findById(1L)).thenReturn(Optional.of(profilStandard));
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurResponse response = service.creerUtilisateur(creerRequest());

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getProfilNom()).isEqualTo("UTILISATEUR_STANDARD");

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        Utilisateur enregistre = captor.getValue();
        assertThat(enregistre.getPassword()).isNotEqualTo(MOT_DE_PASSE);
        assertThat(passwordEncoder.matches(MOT_DE_PASSE, enregistre.getPassword())).isTrue();
        assertThat(enregistre.getProfil().getId()).isEqualTo(1L);
        assertThat(enregistre.getDateCreation()).isNotNull();
        assertThat(enregistre.getDateModification()).isNotNull();
    }

    @Test
    void creerUtilisateur_emailDoublon_refuse() {
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.creerUtilisateur(creerRequest()))
                .isInstanceOf(EmailDejaUtiliseException.class);

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void creerUtilisateur_profilParDefaut_siAbsent() {
        CreateUtilisateurRequest request = creerRequest();
        request.setProfilId(null);
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(profilRepository.findByNom("UTILISATEUR_STANDARD")).thenReturn(Optional.of(profilStandard));
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurResponse response = service.creerUtilisateur(request);

        assertThat(response.getProfilNom()).isEqualTo("UTILISATEUR_STANDARD");
    }

    @Test
    void modifierUtilisateur_neModifiePasLeProfil() {
        Profil profilAdmin = new Profil();
        profilAdmin.setId(2L);
        profilAdmin.setNom("ADMINISTRATEUR");

        Utilisateur existant = new Utilisateur();
        existant.setId(10L);
        existant.setNom("Dupont");
        existant.setPrenom("Jean");
        existant.setEmail(EMAIL);
        existant.setPassword(passwordEncoder.encode(MOT_DE_PASSE));
        existant.setProfil(profilAdmin);

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(existant));
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUtilisateurRequest request = new UpdateUtilisateurRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean-Marc");
        request.setEmail(EMAIL);
        request.setTelephone("0700000000");

        UtilisateurResponse response = service.modifierUtilisateur(10L, request);

        assertThat(response.getPrenom()).isEqualTo("Jean-Marc");
        assertThat(response.getProfilNom()).isEqualTo("ADMINISTRATEUR");
    }

    @Test
    void modifierUtilisateur_autreUtilisateur_refuse() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);

        UpdateUtilisateurRequest request = new UpdateUtilisateurRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean-Marc");
        request.setEmail(EMAIL);

        assertThatThrownBy(() -> service.modifierUtilisateur(10L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(999L))
                .isInstanceOf(UtilisateurNonTrouveException.class);
    }

    @Test
    void trouverParId_existant_retourneReponse() {
        Utilisateur existant = new Utilisateur();
        existant.setId(1L);
        existant.setNom("Dupont");
        existant.setPrenom("Jean");
        existant.setEmail(EMAIL);
        existant.setPassword(passwordEncoder.encode(MOT_DE_PASSE));
        existant.setProfil(profilStandard);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(existant));

        UtilisateurResponse response = service.trouverParId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getProfilNom()).isEqualTo("UTILISATEUR_STANDARD");
    }

    @Test
    void trouverParEmail_inexistant_leveException() {
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParEmail(EMAIL))
                .isInstanceOf(UtilisateurNonTrouveException.class);
    }

    @Test
    void trouverTous_retourneUtilisateurs() {
        when(utilisateurRepository.findAll()).thenReturn(java.util.List.of());
        assertThat(service.trouverTous()).isEmpty();
    }

    @Test
    void rechercher_sansMotCle_retournePage() {
        when(utilisateurRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        Page<?> page = service.rechercher(new SearchRequest());
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
    }

    // ---------------------------------------------------------------------
    // Exceptions RBAC individuelles (ACCORDER / REFUSER)
    // ---------------------------------------------------------------------

    private Utilisateur creerUtilisateurExistant() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setNom("Dupont");
        utilisateur.setPrenom("Jean");
        utilisateur.setEmail(EMAIL);
        utilisateur.setPassword(passwordEncoder.encode(MOT_DE_PASSE));
        utilisateur.setProfil(profilStandard);
        return utilisateur;
    }

    @Test
    void accorderPermission_creeExceptionAccorder() {
        Utilisateur utilisateur = creerUtilisateurExistant();
        com.cms.permission.entity.Permission permission = new com.cms.permission.entity.Permission();
        permission.setId(5L);
        permission.setNomPermission("CREER_TACHE");

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(utilisateurPermissionRepository.findByUtilisateurIdAndPermissionIdAndType(
                10L, 5L, com.cms.utilisateur.entity.enums.UtilisateurPermissionType.ACCORDER))
                .thenReturn(Optional.empty());
        when(utilisateurPermissionRepository.save(any(com.cms.utilisateur.entity.UtilisateurPermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.accorderPermission(10L, 5L);

        assertThat(response.getUtilisateurId()).isEqualTo(10L);
        assertThat(response.getNomPermission()).isEqualTo("CREER_TACHE");
        assertThat(response.getType()).isEqualTo(com.cms.utilisateur.entity.enums.UtilisateurPermissionType.ACCORDER);
    }

    @Test
    void accorderPermission_dejaAccordee_leveException() {
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(creerUtilisateurExistant()));
        com.cms.permission.entity.Permission permission = new com.cms.permission.entity.Permission();
        permission.setId(5L);
        permission.setNomPermission("CREER_TACHE");
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(utilisateurPermissionRepository.findByUtilisateurIdAndPermissionIdAndType(
                10L, 5L, com.cms.utilisateur.entity.enums.UtilisateurPermissionType.ACCORDER))
                .thenReturn(Optional.of(new com.cms.utilisateur.entity.UtilisateurPermission()));

        assertThatThrownBy(() -> service.accorderPermission(10L, 5L))
                .isInstanceOf(com.cms.exception.custom.PermissionDejaAttribueeException.class);
    }

    @Test
    void refuserPermission_creeExceptionRefuser() {
        Utilisateur utilisateur = creerUtilisateurExistant();
        com.cms.permission.entity.Permission permission = new com.cms.permission.entity.Permission();
        permission.setId(6L);
        permission.setNomPermission("VOIR_DOCUMENT");

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findById(6L)).thenReturn(Optional.of(permission));
        when(utilisateurPermissionRepository.findByUtilisateurIdAndPermissionIdAndType(
                10L, 6L, com.cms.utilisateur.entity.enums.UtilisateurPermissionType.REFUSER))
                .thenReturn(Optional.empty());
        when(utilisateurPermissionRepository.save(any(com.cms.utilisateur.entity.UtilisateurPermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.refuserPermission(10L, 6L);

        assertThat(response.getType()).isEqualTo(com.cms.utilisateur.entity.enums.UtilisateurPermissionType.REFUSER);
    }

    @Test
    void listerPermissionsIndividuelles_retourneExceptions() {
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(creerUtilisateurExistant()));
        when(utilisateurPermissionRepository.findByUtilisateurId(10L)).thenReturn(java.util.List.of());

        assertThat(service.listerPermissionsIndividuelles(10L)).isEmpty();
    }
}

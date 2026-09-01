package com.cms.security.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilPermissionResponse;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.entity.Profil;
import com.cms.profil.entity.ProfilPermission;
import com.cms.profil.mapper.ProfilMapperImpl;
import com.cms.profil.mapper.ProfilPermissionMapperImpl;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.profil.repository.ProfilRepository;
import com.cms.profil.service.ProfilServiceImpl;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.UtilisateurPermission;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests de l'administration RBAC (LOOP 3.12) — les 5 cas obligatoires :
 *
 * <p>Cas 1 : creer un profil → profil cree.
 * Cas 2 : associer une permission → permission disponible pour le profil.
 * Cas 3 : utilisateur avec profil → herite des permissions du profil.
 * Cas 4 : utilisateur ACCORDER → permission supplementaire.
 * Cas 5 : utilisateur REFUSER → permission retiree, meme si le profil l'autorise.
 *
 * <p>Le calcul des droits effectifs (formule profil + ACCORDER - REFUSER,
 * REFUSER prioritaire) est verifie via {@code DroitsService} ; la gestion
 * profil / association via {@code ProfilService}.
 */
@ExtendWith(MockitoExtension.class)
class RbacAdministrationTest {

    private static final long UTILISATEUR_ID = 10L;

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ProfilRepository profilRepository;
    @Mock
    private ProfilPermissionRepository profilPermissionRepository;
    @Mock
    private UtilisateurPermissionRepository utilisateurPermissionRepository;
    @Mock
    private com.cms.chantier.repository.UtilisateurPermissionChantierRepository utilisateurPermissionChantierRepository;
    @Mock
    private com.cms.chantier.repository.AffectationUtilisateurChantierRepository affectationUtilisateurChantierRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private ProfilServiceImpl profilService;
    private DroitsService droitsService;

    @BeforeEach
    void setUp() {
        profilService = new ProfilServiceImpl(profilRepository, profilPermissionRepository,
                permissionRepository, new ProfilMapperImpl(), new ProfilPermissionMapperImpl());
        droitsService = new DroitsService(utilisateurRepository, profilPermissionRepository,
                utilisateurPermissionRepository, utilisateurPermissionChantierRepository,
                affectationUtilisateurChantierRepository, permissionRepository);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Permission permission(String code) {
        Permission p = new Permission();
        p.setId((long) code.hashCode());
        p.setNom(code);
        p.setNomPermission(code);
        String[] parts = code.split("_", 2);
        p.setModule(parts[0]);
        return p;
    }

    private Profil profilStandard() {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("RESPONSABLE_CHANTIER");
        return profil;
    }

    private Utilisateur utilisateurAvecProfil(Profil profil) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UTILISATEUR_ID);
        utilisateur.setEmail("responsable@example.com");
        utilisateur.setProfil(profil);
        return utilisateur;
    }

    private ProfilPermission profilPermission(Profil profil, Permission permission) {
        ProfilPermission pp = new ProfilPermission();
        pp.setProfil(profil);
        pp.setPermission(permission);
        return pp;
    }

    private UtilisateurPermission exceptionIndividuelle(Permission permission, UtilisateurPermissionType type) {
        UtilisateurPermission up = new UtilisateurPermission();
        up.setPermission(permission);
        up.setType(type);
        return up;
    }

    // ---------------------------------------------------------------------
    // Cas obligatoires
    // ---------------------------------------------------------------------

    @Test
    void cas1_creerProfil_profilCree() {
        when(profilRepository.existsByNom("RESPONSABLE_CHANTIER")).thenReturn(false);
        when(profilRepository.save(any(Profil.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateProfilRequest request = new CreateProfilRequest();
        request.setNom("RESPONSABLE_CHANTIER");
        request.setDescription("Responsable de chantier");

        ProfilResponse response = profilService.creerProfil(request);

        assertThat(response.getNom()).isEqualTo("RESPONSABLE_CHANTIER");
    }

    @Test
    void cas2_attribuerPermission_profilPossedeLaPermission() {
        Profil profil = profilStandard();
        Permission permission = permission("TACHE_LIRE");

        when(profilRepository.findById(1L)).thenReturn(Optional.of(profil));
        when(permissionRepository.findById((long) "TACHE_LIRE".hashCode())).thenReturn(Optional.of(permission));
        when(profilPermissionRepository.findByProfilIdAndPermissionId(1L, (long) "TACHE_LIRE".hashCode()))
                .thenReturn(Optional.empty());
        when(profilPermissionRepository.save(any(ProfilPermission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProfilPermissionResponse association = profilService.ajouterPermission(1L, (long) "TACHE_LIRE".hashCode());

        assertThat(association.getProfilId()).isEqualTo(1L);
        assertThat(association.getNomPermission()).isEqualTo("TACHE_LIRE");
    }

    @Test
    void cas3_utilisateurAvecProfil_heriteDesPermissions() {
        Profil profil = profilStandard();
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TACHE_LIRE")),
                profilPermission(profil, permission("TACHE_CREER"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).containsExactlyInAnyOrder("TACHE_LIRE", "TACHE_CREER");
    }

    @Test
    void cas4_utilisateurAccorder_obtientPermissionSupplementaire() {
        Profil profil = profilStandard();
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TACHE_LIRE"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("CHANTIER_VALIDER"), UtilisateurPermissionType.ACCORDER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).containsExactlyInAnyOrder("TACHE_LIRE", "CHANTIER_VALIDER");
    }

    @Test
    void cas5_utilisateurRefuser_permissionRetireeMemeSiProfilAutorise() {
        Profil profil = profilStandard();
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TACHE_LIRE")),
                profilPermission(profil, permission("TACHE_SUPPRIMER"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TACHE_SUPPRIMER"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).contains("TACHE_LIRE");
        assertThat(droits).doesNotContain("TACHE_SUPPRIMER");
    }

}

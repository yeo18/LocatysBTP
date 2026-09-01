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
import com.cms.profil.entity.Profil;
import com.cms.profil.entity.ProfilPermission;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.UtilisateurPermission;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests du calcul des droits effectifs (RBAC dynamique) — OBJECTIF 9.
 *
 * <p>Cas 1 : ADMINISTRATEUR = toutes les permissions du referentiel.
 * Cas 2 : UTILISATEUR_STANDARD = uniquement les permissions attribuees.
 * Cas 3 : ACCORDER ajoute une permission individuelle.
 * Cas 4 : REFUSER retire une permission, meme si le profil l'autorise.
 */
@ExtendWith(MockitoExtension.class)
class DroitsServiceTest {

    private static final long UTILISATEUR_ID = 10L;

    @Mock
    private UtilisateurRepository utilisateurRepository;
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

    private DroitsService service;

    @BeforeEach
    void setUp() {
        service = new DroitsService(utilisateurRepository, profilPermissionRepository,
                utilisateurPermissionRepository, utilisateurPermissionChantierRepository,
                affectationUtilisateurChantierRepository, permissionRepository);
    }

    private Permission permission(String code) {
        Permission p = new Permission();
        p.setId((long) code.hashCode());
        p.setNom(code);
        p.setNomPermission(code);
        String[] parts = code.split("_", 2);
        p.setModule(parts[0]);
        return p;
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

    private Utilisateur utilisateurAvecProfil(String nomProfil) {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom(nomProfil);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UTILISATEUR_ID);
        utilisateur.setEmail("utilisateur@example.com");
        utilisateur.setProfil(profil);
        return utilisateur;
    }

    @Test
    void cas1_administrateur_possedeToutesLesPermissions() {
        Utilisateur utilisateur = utilisateurAvecProfil("ADMINISTRATEUR");
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findAll()).thenReturn(List.of(
                permission("TACHE_LIRE"), permission("TACHE_CREER"), permission("CHANTIER_LIRE")));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = service.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).containsExactlyInAnyOrder("TACHE_LIRE", "TACHE_CREER", "CHANTIER_LIRE");
    }

    @Test
    void cas2_utilisateurStandard_uniquementLesPermissionsAttribuees() {
        Utilisateur utilisateur = utilisateurAvecProfil("UTILISATEUR_STANDARD");
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(utilisateur.getProfil(), permission("TACHE_LIRE")),
                profilPermission(utilisateur.getProfil(), permission("CHANTIER_LIRE"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = service.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).containsExactlyInAnyOrder("TACHE_LIRE", "CHANTIER_LIRE");
        assertThat(droits).doesNotContain("TACHE_CREER");
    }

    @Test
    void cas3_accorder_ajouteUnePermissionIndividuelle() {
        Utilisateur utilisateur = utilisateurAvecProfil("UTILISATEUR_STANDARD");
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(utilisateur.getProfil(), permission("VOIR_TACHE"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("CREER_TACHE"), UtilisateurPermissionType.ACCORDER)));

        Set<String> droits = service.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).containsExactlyInAnyOrder("VOIR_TACHE", "CREER_TACHE");
    }

    @Test
    void cas4_refuser_retireUnePermissionMemeSiLeProfilLAutorise() {
        Utilisateur utilisateur = utilisateurAvecProfil("CHEF_EQUIPE");
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(utilisateur.getProfil(), permission("VOIR_DOCUMENT"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("VOIR_DOCUMENT"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = service.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).doesNotContain("VOIR_DOCUMENT");
        assertThat(droits).isEmpty();
    }

    @Test
    void cas4bis_refuserEstPrioritaireSurAdmin() {
        Utilisateur utilisateur = utilisateurAvecProfil("ADMINISTRATEUR");
        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findAll()).thenReturn(List.of(
                permission("TACHE_LIRE"), permission("TACHE_VALIDER")));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TACHE_VALIDER"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = service.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).contains("TACHE_LIRE");
        assertThat(droits).doesNotContain("TACHE_VALIDER");
    }

}

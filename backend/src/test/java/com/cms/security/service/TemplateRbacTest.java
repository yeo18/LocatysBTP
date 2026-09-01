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
 * Tests RBAC du module Template (LOOP 5.11).
 *
 * <p>Verifie que les permissions {@code TEMPLATE_*} suivent exactement le
 * mecanisme RBAC dynamique existant (formule profil + ACCORDER - REFUSER,
 * REFUSER prioritaire) — aucun systeme parallele, aucun role code en dur.
 *
 * <p>Cas couverts : ACCORDER (9), REFUSER (10), REFUSER utilisateur
 * prioritaire sur ACCORDER profil (11), ADMINISTRATEUR => droits du
 * referentiel y compris TEMPLATE_* (12), heritage profil (3), permission
 * de creation du module Template.
 */
@ExtendWith(MockitoExtension.class)
class TemplateRbacTest {

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

    private DroitsService droitsService;

    @BeforeEach
    void setUp() {
        droitsService = new DroitsService(utilisateurRepository, profilPermissionRepository,
                utilisateurPermissionRepository, utilisateurPermissionChantierRepository,
                affectationUtilisateurChantierRepository, permissionRepository);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Permission permission(String code) {
        Permission p = new Permission();
        p.setId((long) code.hashCode());
        p.setNom(code);
        p.setNomPermission(code);
        String[] parts = code.split("_", 2);
        p.setModule(parts[0]);
        return p;
    }

    private Profil profil(String nom) {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom(nom);
        return profil;
    }

    private Utilisateur utilisateurAvecProfil(Profil profil) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UTILISATEUR_ID);
        utilisateur.setEmail("utilisateur@example.com");
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

    // ------------------------------------------------------------------
    // 3. heritage profil : utilisateur autorise a creer un template
    // ------------------------------------------------------------------

    @Test
    void utilisateurAutorise_heriteTEMPLATE_CREER() {
        Profil profil = profil("RESPONSABLE_CHANTIER");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TEMPLATE_CHANTIER_CREER")),
                profilPermission(profil, permission("TEMPLATE_CHANTIER_LIRE"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).contains("TEMPLATE_CHANTIER_CREER", "TEMPLATE_CHANTIER_LIRE");
    }

    // ------------------------------------------------------------------
    // 9. ACCORDER -> permission accordee
    // ------------------------------------------------------------------

    @Test
    void accorder_ajoutePermissionTemplate() {
        Profil profil = profil("OUVRIER");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of());
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TEMPLATE_TACHE_CREER"), UtilisateurPermissionType.ACCORDER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).contains("TEMPLATE_TACHE_CREER");
    }

    // ------------------------------------------------------------------
    // 10. REFUSER -> permission refusee
    // ------------------------------------------------------------------

    @Test
    void refuser_retirePermissionTemplate() {
        Profil profil = profil("RESPONSABLE_CHANTIER");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TEMPLATE_CHANTIER_MODIFIER"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TEMPLATE_CHANTIER_MODIFIER"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).doesNotContain("TEMPLATE_CHANTIER_MODIFIER");
    }

    // ------------------------------------------------------------------
    // 11. REFUSER utilisateur prioritaire sur ACCORDER profil
    // ------------------------------------------------------------------

    @Test
    void refuserUtilisateur_prioritaireSurAccorderProfil() {
        // Le profil donne TEMPLATE_CHANTIER_CREER, mais l'utilisateur a un
        // REFUSER individuel -> REFUSER gagne (priorite documentee RBAC).
        Profil profil = profil("RESPONSABLE_CHANTIER");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TEMPLATE_CHANTIER_CREER"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TEMPLATE_CHANTIER_CREER"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).doesNotContain("TEMPLATE_CHANTIER_CREER");
    }

    @Test
    void accorderProfil_refuseUtilisateur_neChangePasLesAutresPermissions() {
        Profil profil = profil("RESPONSABLE_CHANTIER");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(profilPermissionRepository.findByProfilId(1L)).thenReturn(List.of(
                profilPermission(profil, permission("TEMPLATE_CHANTIER_CREER")),
                profilPermission(profil, permission("TEMPLATE_TACHE_LIRE"))));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of(
                exceptionIndividuelle(permission("TEMPLATE_CHANTIER_CREER"), UtilisateurPermissionType.REFUSER)));

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).doesNotContain("TEMPLATE_CHANTIER_CREER");
        assertThat(droits).contains("TEMPLATE_TACHE_LIRE");
    }

    // ------------------------------------------------------------------
    // 12. ADMINISTRATEUR -> droits du referentiel (y compris TEMPLATE_*)
    // ------------------------------------------------------------------

    @Test
    void administrateur_obtientToutesLesPermissionsDuReferentiel() {
        Profil profil = profil("ADMINISTRATEUR");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        // Le referentiel contient les permissions existantes + TEMPLATE_*
        when(permissionRepository.findAll()).thenReturn(List.of(
                permission("TACHE_LIRE"),
                permission("TEMPLATE_CHANTIER_LIRE"),
                permission("TEMPLATE_CHANTIER_CREER"),
                permission("TEMPLATE_CHANTIER_MODIFIER"),
                permission("TEMPLATE_TACHE_LIRE"),
                permission("TEMPLATE_TACHE_CREER"),
                permission("TEMPLATE_TACHE_MODIFIER")));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits)
                .contains("TEMPLATE_CHANTIER_LIRE", "TEMPLATE_CHANTIER_CREER", "TEMPLATE_CHANTIER_MODIFIER")
                .contains("TEMPLATE_TACHE_LIRE", "TEMPLATE_TACHE_CREER", "TEMPLATE_TACHE_MODIFIER")
                .contains("TACHE_LIRE");
    }

    @Test
    void administrateur_aucunePermissionTemplateEnDur() {
        // Le mecanisme est dynamique : si le referentiel ne contient pas une
        // permission Template, l'ADMINISTRATEUR ne l'obtient pas non plus.
        Profil profil = profil("ADMINISTRATEUR");
        Utilisateur utilisateur = utilisateurAvecProfil(profil);

        when(utilisateurRepository.findById(UTILISATEUR_ID)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findAll()).thenReturn(List.of(permission("TACHE_LIRE")));
        when(utilisateurPermissionRepository.findByUtilisateurId(UTILISATEUR_ID)).thenReturn(List.of());

        Set<String> droits = droitsService.calculerDroits(UTILISATEUR_ID);

        assertThat(droits).doesNotContain("TEMPLATE_CHANTIER_CREER");
    }

}

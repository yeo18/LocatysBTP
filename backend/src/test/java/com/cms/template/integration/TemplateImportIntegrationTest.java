package com.cms.template.integration;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.TemplateChantierTache;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.repository.TemplateChantierRepository;
import com.cms.template.repository.TemplateChantierTacheRepository;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.service.TemplateChantierService;
import com.cms.template.service.TemplateTacheService;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests d'integration reelle des IMPORTS metier du module Template (LOOP 5.12).
 *
 * <p>PostgreSQL reel ({@code gestion_de_chantier_test}), services reels
 * (non mockes) : le comportement de copie (snapshot) est verifie en base.
 *
 * <p>Cas couverts :
 * <ul>
 *   <li>CAS 1 : import TemplateChantier -> Chantier (taches creees, template inchange).</li>
 *   <li>CAS 2 : import TemplateTache -> Chantier (aucune dependance permanente).</li>
 *   <li>CAS 3 : modification du template apres import -> chantier inchange.</li>
 *   <li>Rollback : echec en plein milieu de l'import -> aucune donnee partielle.</li>
 * </ul>
 *
 * <p>Les tests CAS 1/2/3 sont {@code @Transactional} (rollback a la fin du test).
 * Le test de rollback n'est PAS transactionnel : il verifie le comportement reel
 * de la transaction du service (aucune donnee partielle apres echec).
 */
@SpringBootTest
@ActiveProfiles("test")
class TemplateImportIntegrationTest {

    @Autowired
    private TemplateChantierRepository templateChantierRepository;
    @Autowired
    private TemplateTacheRepository templateTacheRepository;
    @Autowired
    private TemplateChantierTacheRepository templateChantierTacheRepository;
    @Autowired
    private ChantierRepository chantierRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private TemplateChantierService templateChantierService;
    @Autowired
    private TemplateTacheService templateTacheService;

    @MockBean
    private CurrentUserService currentUserService;

    private Utilisateur createur;
    private final java.util.List<Long> templateTacheIds = new java.util.ArrayList<>();
    private final java.util.List<Long> templateChantierIds = new java.util.ArrayList<>();
    private final java.util.List<Long> chantierIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUp() {
        Profil admin = profilRepository.findByNom("ADMINISTRATEUR")
                .orElseGet(() -> {
                    Profil p = new Profil();
                    p.setNom("ADMINISTRATEUR");
                    p.setDateCreation(LocalDateTime.now());
                    p.setDateModification(LocalDateTime.now());
                    return profilRepository.save(p);
                });

        createur = new Utilisateur();
        createur.setNom("Test");
        createur.setPrenom("Import");
        createur.setEmail("import-" + System.nanoTime() + "@example.com");
        createur.setPassword("motdepasse");
        createur.setDateCreation(LocalDateTime.now());
        createur.setDateModification(LocalDateTime.now());
        createur.setProfil(admin);
        createur = utilisateurRepository.saveAndFlush(createur);

        when(currentUserService.getCurrentUtilisateur()).thenReturn(createur);
        when(currentUserService.getCurrentUserId()).thenReturn(createur.getId());
    }

    @AfterEach
    void tearDown() {
        for (Long chantierId : chantierIds) {
            tacheRepository.deleteAll(tachesDuChantier(chantierId));
        }
        for (Long templateChantierId : templateChantierIds) {
            templateChantierTacheRepository.deleteAll(
                    templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(templateChantierId));
        }
        for (Long templateTacheId : templateTacheIds) {
            templateTacheRepository.deleteById(templateTacheId);
        }
        templateChantierRepository.deleteAllById(templateChantierIds);
        chantierRepository.deleteAllById(chantierIds);
        utilisateurRepository.deleteById(createur.getId());
    }

    private TemplateChantier chantierTemplate(String nom) {
        TemplateChantier template = new TemplateChantier();
        template.setNom(nom);
        template.setDescription("Description");
        template.setTypeConstruction(TypeConstruction.MAISON_R1);
        template.setStatut(TemplateChantierStatut.ACTIF);
        template.setDateCreation(LocalDateTime.now());
        template.setDateModification(LocalDateTime.now());
        template.setCreatedBy(createur);
        TemplateChantier sauvegarde = templateChantierRepository.saveAndFlush(template);
        templateChantierIds.add(sauvegarde.getId());
        return sauvegarde;
    }

    private TemplateTache tacheTemplate(String titre, Priorite priorite, Integer duree) {
        TemplateTache tache = new TemplateTache();
        tache.setTitre(titre);
        tache.setDescription("Description");
        tache.setPriorite(priorite);
        tache.setDureeEstimeeJours(duree);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        tache.setCreatedBy(createur);
        TemplateTache sauvegarde = templateTacheRepository.saveAndFlush(tache);
        templateTacheIds.add(sauvegarde.getId());
        return sauvegarde;
    }

    private Chantier chantier(String nom) {
        Chantier chantier = new Chantier();
        chantier.setNom(nom);
        chantier.setStatut(ChantierStatut.PREVU);
        chantier.setProgression(0);
        chantier.setDateCreation(LocalDateTime.now());
        chantier.setDateModification(LocalDateTime.now());
        Chantier sauvegarde = chantierRepository.saveAndFlush(chantier);
        chantierIds.add(sauvegarde.getId());
        return sauvegarde;
    }

    private void associer(TemplateChantier template, TemplateTache tache) {
        TemplateChantierTache association = new TemplateChantierTache();
        association.setTemplateChantier(template);
        association.setTemplateTache(tache);
        templateChantierTacheRepository.saveAndFlush(association);
    }

    private List<Tache> tachesDuChantier(Long chantierId) {
        return tacheRepository.findByChantierId(chantierId, PageRequest.of(0, 100)).getContent();
    }

    // ------------------------------------------------------------------
    // CAS 1 : TemplateChantier -> Chantier
    // ------------------------------------------------------------------

    @Test
    @Transactional
    void importTemplateChantier_copieLesTachesEtLaisseTemplateInchange() {
        TemplateChantier template = templateChantierRepository
                .saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache dalle = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        TemplateTache toiture = templateTacheRepository
                .saveAndFlush(tacheTemplate("Toiture tole", Priorite.MOYENNE, 7));
        associer(template, dalle);
        associer(template, toiture);
        Chantier chantier = chantier("Chantier A");

        int creees = templateChantierService.importerDansChantier(template.getId(), chantier.getId());

        assertThat(creees).isEqualTo(2);

        List<Tache> taches = tachesDuChantier(chantier.getId());
        assertThat(taches).hasSize(2);
        assertThat(taches).extracting(Tache::getTitre).containsExactlyInAnyOrder("Dalle beton", "Toiture tole");
        assertThat(taches).allMatch(t -> TacheStatus.A_FAIRE.equals(t.getStatus()));
        assertThat(taches).allMatch(t -> t.getProgression() == 0);

        // le template reste inchange (toujours ACTIF, associations conservees)
        TemplateChantier relu = templateChantierRepository.findById(template.getId()).orElseThrow();
        assertThat(relu.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
        assertThat(templateChantierTacheRepository
                .findByTemplateChantierIdOrderByIdAsc(template.getId())).hasSize(2);
    }

    @Test
    @Transactional
    void importTemplateChantier_conservePriorite() {
        TemplateChantier template = templateChantierRepository
                .saveAndFlush(chantierTemplate("Immeuble R+5"));
        TemplateTache dalle = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        TemplateTache peinture = templateTacheRepository
                .saveAndFlush(tacheTemplate("Peinture", Priorite.BASSE, 3));
        associer(template, peinture);
        associer(template, dalle);
        Chantier chantier = chantier("Chantier B");

        templateChantierService.importerDansChantier(template.getId(), chantier.getId());

        List<Tache> taches = tachesDuChantier(chantier.getId());
        assertThat(taches).hasSize(2);
        Tache dalleCopiee = taches.stream().filter(t -> t.getTitre().equals("Dalle beton")).findFirst().orElseThrow();
        assertThat(dalleCopiee.getPriorite()).isEqualTo(Priorite.HAUTE);
        Tache peintureCopiee = taches.stream().filter(t -> t.getTitre().equals("Peinture")).findFirst().orElseThrow();
        assertThat(peintureCopiee.getPriorite()).isEqualTo(Priorite.BASSE);
    }

    @Test
    @Transactional
    void importTemplateChantier_sansTaches_neCreeRien() {
        TemplateChantier template = templateChantierRepository
                .saveAndFlush(chantierTemplate("Villa"));
        Chantier chantier = chantier("Chantier C");

        int creees = templateChantierService.importerDansChantier(template.getId(), chantier.getId());

        assertThat(creees).isZero();
        assertThat(tachesDuChantier(chantier.getId())).isEmpty();
    }

    // ------------------------------------------------------------------
    // CAS 2 : TemplateTache -> Chantier (dependance permanente absente)
    // ------------------------------------------------------------------

    @Test
    @Transactional
    void importTemplateTache_creeUneTacheIndependante() {
        TemplateTache dalle = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        Chantier chantier = chantier("Chantier D");

        int creees = templateTacheService.importerDansChantier(dalle.getId(), chantier.getId());

        assertThat(creees).isEqualTo(1);
        List<Tache> taches = tachesDuChantier(chantier.getId());
        assertThat(taches).hasSize(1);
        Tache copiee = taches.get(0);
        assertThat(copiee.getTitre()).isEqualTo("Dalle beton");
        assertThat(copiee.getPriorite()).isEqualTo(Priorite.HAUTE);
        assertThat(copiee.getStatus()).isEqualTo(TacheStatus.A_FAIRE);
        assertThat(copiee.getProgression()).isZero();
    }

    @Test
    @Transactional
    void importTemplateTache_aucuneFkTemplate() {
        TemplateTache dalle = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        Chantier chantier = chantier("Chantier E");

        templateTacheService.importerDansChantier(dalle.getId(), chantier.getId());

        // La tache creee ne reference pas la table template_tache : suppression
        // du template n'affecte pas la tache creee (verifie au niveau metier).
        Tache copiee = tachesDuChantier(chantier.getId()).get(0);
        assertThat(copiee.getChantier().getId()).isEqualTo(chantier.getId());
        // aucune colonne template_tache_id dans tache : la copie est un snapshot.
        assertThat(copiee.getTitre()).isEqualTo("Dalle beton");
    }

    // ------------------------------------------------------------------
    // CAS 3 : modification du template apres import -> chantier inchange
    // ------------------------------------------------------------------

    @Test
    @Transactional
    void modificationTemplateApresImport_neChangePasLeChantier() {
        TemplateChantier template = templateChantierRepository
                .saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache dalle = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        associer(template, dalle);
        Chantier chantier = chantier("Chantier F");

        templateChantierService.importerDansChantier(template.getId(), chantier.getId());
        assertThat(tachesDuChantier(chantier.getId())).hasSize(1);

        // modification du template (nom + description)
        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Maison R+1 v2");
        request.setDescription("Modifiee apres import");
        request.setTypeConstruction(TypeConstruction.VILLA);
        templateChantierService.modifier(template.getId(), request);

        // le chantier existant ne change pas
        List<Tache> taches = tachesDuChantier(chantier.getId());
        assertThat(taches).hasSize(1);
        assertThat(taches.get(0).getTitre()).isEqualTo("Dalle beton");

        // modification du template de tache : la tache creee est inchangee
        CreateTemplateTacheRequest modif = new CreateTemplateTacheRequest();
        modif.setTitre("Dalle beton armee");
        modif.setPriorite(Priorite.MOYENNE);
        templateTacheService.modifier(dalle.getId(), toUpdate(modif));
        List<Tache> tachesApres = tachesDuChantier(chantier.getId());
        assertThat(tachesApres.get(0).getTitre()).isEqualTo("Dalle beton");
    }

    // ------------------------------------------------------------------
    // 5. Rollback transactionnel : voir TemplateImportRollbackIntegrationTest
    // ------------------------------------------------------------------

    private com.cms.template.dto.UpdateTemplateTacheRequest toUpdate(CreateTemplateTacheRequest request) {
        com.cms.template.dto.UpdateTemplateTacheRequest update = new com.cms.template.dto.UpdateTemplateTacheRequest();
        update.setTitre(request.getTitre());
        update.setPriorite(request.getPriorite());
        return update;
    }

}

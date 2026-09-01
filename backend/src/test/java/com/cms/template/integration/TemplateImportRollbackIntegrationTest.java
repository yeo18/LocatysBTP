package com.cms.template.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.TemplateChantierTache;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.repository.TemplateChantierRepository;
import com.cms.template.repository.TemplateChantierTacheRepository;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.service.TemplateChantierService;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests d'integration du ROLLBACK transactionnel de l'import (LOOP 5.12).
 *
 * <p>PostgreSQL reel ({@code gestion_de_chantier_test}). Le test n'est PAS
 * {@code @Transactional} : il verifie le comportement reel de la transaction
 * du service {@code importerDansChantier} (@Transactional).
 *
 * <p>Le repository {@code TacheRepository} est mocke afin de controler
 * precisement le moment de l'echec : la 1re tache est reellement inseree en
 * base via {@link JdbcTemplate} (dans la transaction du service), la 2e leve
 * une exception. Le rollback doit annuler la 1re insertion reelle.
 *
 * <p>Les donnees de setup (template, chantier, utilisateur) sont commitees et
 * nettoyees par {@code tearDown} (test non transactionnel).
 */
@SpringBootTest
@ActiveProfiles("test")
class TemplateImportRollbackIntegrationTest {

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
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TemplateChantierService templateChantierService;

    @MockBean
    private TacheRepository tacheRepository;
    @MockBean
    private CurrentUserService currentUserService;

    private Utilisateur createur;
    private final List<Long> templateTacheIds = new ArrayList<>();
    private final List<Long> templateChantierIds = new ArrayList<>();
    private final List<Long> chantierIds = new ArrayList<>();

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
        createur.setPrenom("Rollback");
        createur.setEmail("rollback-" + System.nanoTime() + "@example.com");
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
            jdbcTemplate.update("DELETE FROM tache WHERE chantier_id = ?", chantierId);
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

    @Test
    void importAvecErreurAuMilieu_rollbackComplet_aucuneDonneePartielle() {
        TemplateChantier template = templateChantierRepository
                .saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache t1 = templateTacheRepository
                .saveAndFlush(tacheTemplate("Dalle beton", Priorite.HAUTE, 5));
        TemplateTache t2 = templateTacheRepository
                .saveAndFlush(tacheTemplate("Toiture tole", Priorite.MOYENNE, 7));
        associer(template, t1);
        associer(template, t2);
        Chantier chantier = chantier("Chantier ROLLBACK");

        // 1re sauvegarde : insertion SQL reelle (dans la transaction du service).
        // 2e sauvegarde : exception -> la transaction du service doit tout annuler.
        AtomicInteger appels = new AtomicInteger();
        doAnswer(invocation -> {
            if (appels.incrementAndGet() == 2) {
                throw new RuntimeException("Erreur simulee en plein milieu de l'import");
            }
            Tache tache = invocation.getArgument(0);
            jdbcTemplate.update(
                    "INSERT INTO tache (titre, description, priorite, status, progression,"
                            + " date_creation, date_modification, chantier_id, created_by)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    tache.getTitre(), tache.getDescription(),
                    tache.getPriorite() != null ? tache.getPriorite().name() : null,
                    tache.getStatus() != null ? tache.getStatus().name() : null,
                    tache.getProgression(),
                    tache.getDateCreation(), tache.getDateModification(),
                    tache.getChantier().getId(), tache.getCreatedBy().getId());
            return tache;
        }).when(tacheRepository).save(any(Tache.class));

        assertThatThrownBy(() ->
                templateChantierService.importerDansChantier(template.getId(), chantier.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("milieu");

        // ROLLBACK complet : l'insertion SQL reelle de la 1re tache est annulee
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tache WHERE chantier_id = ?", Integer.class, chantier.getId());
        assertThat(count).isZero();

        // le template lui-meme est inchange
        TemplateChantier relu = templateChantierRepository.findById(template.getId()).orElseThrow();
        assertThat(relu.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
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

}

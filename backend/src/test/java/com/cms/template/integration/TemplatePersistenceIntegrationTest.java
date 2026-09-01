package com.cms.template.integration;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.TemplateChantierTache;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.repository.TemplateChantierRepository;
import com.cms.template.repository.TemplateChantierTacheRepository;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests d'integration de PERSISTANCE reelle du module Template (LOOP 5.12).
 *
 * <p>Utilise PostgreSQL reel (base {@code gestion_de_chantier_test}, profil
 * {@code test}) : Flyway applique V1/V2/V3, Hibernate ddl-auto=none. Les
 * contraintes SQL (UNIQUE, FK, NOT NULL, CHECK) sont donc reellement verifiees.
 *
 * <p>Chaque test est {@code @Transactional} : les donnees inserees sont
 * annulees a la fin de chaque test (aucune pollution de la base de test).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemplatePersistenceIntegrationTest {

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

    @MockBean
    private CurrentUserService currentUserService;

    private Utilisateur createur;

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
        createur.setPrenom("Persistance");
        createur.setEmail("persistence-" + System.nanoTime() + "@example.com");
        createur.setPassword("motdepasse");
        createur.setDateCreation(LocalDateTime.now());
        createur.setDateModification(LocalDateTime.now());
        createur.setProfil(admin);
        createur = utilisateurRepository.saveAndFlush(createur);

        when(currentUserService.getCurrentUtilisateur()).thenReturn(createur);
        when(currentUserService.getCurrentUserId()).thenReturn(createur.getId());
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
        return template;
    }

    private TemplateTache tacheTemplate(String titre) {
        TemplateTache tache = new TemplateTache();
        tache.setTitre(titre);
        tache.setDescription("Description");
        tache.setPriorite(Priorite.HAUTE);
        tache.setDureeEstimeeJours(5);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        tache.setCreatedBy(createur);
        return tache;
    }

    private Chantier chantier(String nom) {
        Chantier chantier = new Chantier();
        chantier.setNom(nom);
        chantier.setStatut(ChantierStatut.PREVU);
        chantier.setProgression(0);
        chantier.setDateCreation(LocalDateTime.now());
        chantier.setDateModification(LocalDateTime.now());
        return chantier;
    }

    // ------------------------------------------------------------------
    // 1. Persistance TemplateChantier / TemplateTache
    // ------------------------------------------------------------------

    @Test
    void persisteTemplateChantier_etRelit() {
        TemplateChantier template = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));

        TemplateChantier relu = templateChantierRepository.findById(template.getId()).orElseThrow();
        assertThat(relu.getNom()).isEqualTo("Maison R+1");
        assertThat(relu.getTypeConstruction()).isEqualTo(TypeConstruction.MAISON_R1);
        assertThat(relu.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
        assertThat(relu.getCreatedBy().getId()).isEqualTo(createur.getId());
        assertThat(relu.getDateCreation()).isNotNull();
    }

    @Test
    void persisteTemplateTache_etRelit() {
        TemplateTache tache = templateTacheRepository.saveAndFlush(tacheTemplate("Dalle beton"));

        TemplateTache relue = templateTacheRepository.findById(tache.getId()).orElseThrow();
        assertThat(relue.getTitre()).isEqualTo("Dalle beton");
        assertThat(relue.getPriorite()).isEqualTo(Priorite.HAUTE);
        assertThat(relue.getCreatedBy().getId()).isEqualTo(createur.getId());
    }

    @Test
    void persisteAssociation_etRelitAvecRelations() {
        TemplateChantier template = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache t1 = templateTacheRepository.saveAndFlush(tacheTemplate("Dalle beton"));
        TemplateTache t2 = templateTacheRepository.saveAndFlush(tacheTemplate("Toiture tole"));

        TemplateChantierTache a1 = new TemplateChantierTache();
        a1.setTemplateChantier(template);
        a1.setTemplateTache(t1);
        templateChantierTacheRepository.saveAndFlush(a1);

        TemplateChantierTache a2 = new TemplateChantierTache();
        a2.setTemplateChantier(template);
        a2.setTemplateTache(t2);
        templateChantierTacheRepository.saveAndFlush(a2);

        List<TemplateChantierTache> associations =
                templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(template.getId());

        assertThat(associations).hasSize(2);
        assertThat(associations.get(0).getTemplateTache().getTitre()).isEqualTo("Dalle beton");
        assertThat(associations.get(1).getTemplateTache().getTitre()).isEqualTo("Toiture tole");
    }

    // ------------------------------------------------------------------
    // 2. Contraintes SQL : UNIQUE / FK / NOT NULL / CHECK
    // ------------------------------------------------------------------

    @Test
    void nomUnique_estEnforce() {
        templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));

        assertThatThrownBy(() -> templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void nomNull_violeContrainteNotNull() {
        TemplateChantier template = chantierTemplate(null);

        assertThatThrownBy(() -> templateChantierRepository.saveAndFlush(template))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void typeConstructionNull_violeContrainteNotNull() {
        TemplateChantier template = chantierTemplate("Villa");
        template.setTypeConstruction(null);

        assertThatThrownBy(() -> templateChantierRepository.saveAndFlush(template))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void dureeNegative_violeContrainteCheck() {
        TemplateTache tache = tacheTemplate("Dalle beton");
        tache.setDureeEstimeeJours(-2);

        assertThatThrownBy(() -> templateTacheRepository.saveAndFlush(tache))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void tacheDupliquee_estEnforce() {
        TemplateChantier template = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache t1 = templateTacheRepository.saveAndFlush(tacheTemplate("Dalle beton"));

        TemplateChantierTache a1 = new TemplateChantierTache();
        a1.setTemplateChantier(template);
        a1.setTemplateTache(t1);
        templateChantierTacheRepository.saveAndFlush(a1);

        TemplateChantierTache a2 = new TemplateChantierTache();
        a2.setTemplateChantier(template);
        a2.setTemplateTache(t1);

        assertThatThrownBy(() -> templateChantierTacheRepository.saveAndFlush(a2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void fkTemplateInexistant_violeContrainte() {
        TemplateChantier template = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));

        // FK (fk_template_chantier_tache_tache) : insertion SQL brute d'une
        // association vers un template_tache inexistant -> violation de contrainte.
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO template_chantier_tache (template_chantier_id, template_tache_id) VALUES (?, ?)",
                template.getId(), 999999L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ------------------------------------------------------------------
    // 3. Repository : recherches preexistantes
    // ------------------------------------------------------------------

    @Test
    void repository_findByStatut_retourneActifs() {
        TemplateChantier actif = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateChantier inactif = chantierTemplate("Villa");
        inactif.setStatut(TemplateChantierStatut.INACTIF);
        templateChantierRepository.saveAndFlush(inactif);

        List<TemplateChantier> actifs = templateChantierRepository.findByStatut(TemplateChantierStatut.ACTIF);

        assertThat(actifs).hasSize(1);
        assertThat(actifs.get(0).getId()).isEqualTo(actif.getId());
    }

    @Test
    void repository_findByPriorite_retourneTaches() {
        templateTacheRepository.saveAndFlush(tacheTemplate("Dalle beton"));
        TemplateTache basse = tacheTemplate("Peinture");
        basse.setPriorite(Priorite.BASSE);
        templateTacheRepository.saveAndFlush(basse);

        List<TemplateTache> hautes = templateTacheRepository.findByPriorite(Priorite.HAUTE);

        assertThat(hautes).hasSize(1);
        assertThat(hautes.get(0).getTitre()).isEqualTo("Dalle beton");
    }

    @Test
    void repository_existsByNom_fonctionne() {
        templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));

        assertThat(templateChantierRepository.existsByNom("Maison R+1")).isTrue();
        assertThat(templateChantierRepository.existsByNom("Inexistant")).isFalse();
    }

    @Test
    void repository_existsAssociation_fonctionne() {
        TemplateChantier template = templateChantierRepository.saveAndFlush(chantierTemplate("Maison R+1"));
        TemplateTache t1 = templateTacheRepository.saveAndFlush(tacheTemplate("Dalle beton"));

        TemplateChantierTache a1 = new TemplateChantierTache();
        a1.setTemplateChantier(template);
        a1.setTemplateTache(t1);
        templateChantierTacheRepository.saveAndFlush(a1);

        assertThat(templateChantierTacheRepository
                .existsByTemplateChantierIdAndTemplateTacheId(template.getId(), t1.getId())).isTrue();
        assertThat(templateChantierTacheRepository
                .existsByTemplateChantierIdAndTemplateTacheId(template.getId(), 999999L)).isFalse();
    }

    // ------------------------------------------------------------------
    // 4. Integration chantier/tache (CORE) : FK vers chantier valide
    // ------------------------------------------------------------------

    @Test
    void chantierPersiste_seul_sansPollution() {
        Chantier chantier = chantierRepository.saveAndFlush(chantier("Chantier Test"));

        Chantier relu = chantierRepository.findById(chantier.getId()).orElseThrow();
        assertThat(relu.getNom()).isEqualTo("Chantier Test");
    }

}

package com.cms.template.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.CreateTemplateTacheTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.dto.UpdateTemplateTacheTacheRequest;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.TemplateTacheTache;
import com.cms.template.mapper.TemplateTacheMapper;
import com.cms.template.mapper.TemplateTacheMapperImpl;
import com.cms.template.mapper.TemplateTacheTacheMapper;
import com.cms.template.mapper.TemplateTacheTacheMapperImpl;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.repository.TemplateTacheTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service TemplateTache (repository mocke, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class TemplateTacheServiceImplTest {

    @Mock
    private TemplateTacheRepository templateTacheRepository;
    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private MembreEquipeRepository membreEquipeRepository;
    @Mock
    private AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TemplateTacheTacheRepository templateTacheTacheRepository;

    private TemplateTacheMapper templateTacheMapper = new TemplateTacheMapperImpl();
    private TemplateTacheTacheMapper templateTacheTacheMapper = new TemplateTacheTacheMapperImpl();
    private TemplateTacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TemplateTacheServiceImpl(templateTacheRepository, templateTacheTacheRepository,
                chantierRepository, tacheRepository,
                utilisateurRepository, membreEquipeRepository,
                affectationEquipeChantierRepository, currentUserService,
                templateTacheMapper, templateTacheTacheMapper);
    }

    private TemplateTache templateTache(Long id, String titre) {
        TemplateTache tache = new TemplateTache();
        tache.setId(id);
        tache.setTitre(titre);
        tache.setPriorite(Priorite.HAUTE);
        tache.setDureeEstimeeJours(5);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        return tache;
    }

    private CreateTemplateTacheRequest creerRequest(String titre) {
        CreateTemplateTacheRequest request = new CreateTemplateTacheRequest();
        request.setTitre(titre);
        request.setPriorite(Priorite.HAUTE);
        return request;
    }

    private Chantier chantier(Long id) {
        Chantier chantier = new Chantier();
        chantier.setId(id);
        chantier.setNom("Chantier Test");
        return chantier;
    }

    private void mockAdministrateur(Long id) {
        mockUtilisateur(id, "ADMINISTRATEUR");
    }

    private void mockUtilisateur(Long id, String profilNom) {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom(profilNom);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setProfil(profil);
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateur));
    }

    // ------------------------------------------------------------------
    // D. creer
    // ------------------------------------------------------------------

    @Test
    void creer_fonctionne() {
        when(templateTacheRepository.save(any(TemplateTache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        TemplateTacheResponse response = service.creer(creerRequest("Dalle beton"));

        assertThat(response.getTitre()).isEqualTo("Dalle beton");
        assertThat(response.getPriorite()).isEqualTo(Priorite.HAUTE);
        verify(templateTacheRepository).save(any(TemplateTache.class));
    }

    // ------------------------------------------------------------------
    // E. modifier
    // ------------------------------------------------------------------

    @Test
    void modifier_fonctionne() {
        TemplateTache tache = templateTache(1L, "Dalle beton");
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(templateTacheRepository.save(any(TemplateTache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTemplateTacheRequest request = new UpdateTemplateTacheRequest();
        request.setTitre("Dalle beton armee");
        request.setPriorite(Priorite.MOYENNE);

        TemplateTacheResponse response = service.modifier(1L, request);

        assertThat(response.getTitre()).isEqualTo("Dalle beton armee");
        assertThat(response.getPriorite()).isEqualTo(Priorite.MOYENNE);
    }

    @Test
    void modifier_inexistant_leveException() {
        when(templateTacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(999L, new UpdateTemplateTacheRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // trouverParId / lister
    // ------------------------------------------------------------------

    @Test
    void trouverParId_fonctionne() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));

        TemplateTacheResponse response = service.trouverParId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitre()).isEqualTo("Dalle beton");
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(templateTacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lister_retourneTous() {
        when(templateTacheRepository.findAll()).thenReturn(List.of(
                templateTache(1L, "Dalle beton"),
                templateTache(2L, "Toiture tole")));

        assertThat(service.lister()).hasSize(2);
    }

    // ------------------------------------------------------------------
    // G. importerDansChantier
    // ------------------------------------------------------------------

    @Test
    void importerDansChantier_copieEnTache() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockAdministrateur(10L);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        int creees = service.importerDansChantier(1L, 5L);

        assertThat(creees).isEqualTo(1);
        verify(tacheRepository).save(any(Tache.class));
    }

    @Test
    void importerDansChantier_templateInexistant_leveException() {
        when(templateTacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importerDansChantier(999L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void importerDansChantier_chantierInexistant_leveException() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importerDansChantier(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void importerDansChantier_sansAccesAuChantier_refuse() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockUtilisateur(10L, "UTILISATEUR_STANDARD");
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(membreEquipeRepository.findByUtilisateurId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.importerDansChantier(1L, 5L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void importerDansChantier_membreEquipeAffectee_autorise() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockUtilisateur(10L, "UTILISATEUR_STANDARD");
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        Equipe equipe = new Equipe();
        equipe.setId(2L);
        MembreEquipe membre = new MembreEquipe();
        membre.setEquipe(equipe);
        when(membreEquipeRepository.findByUtilisateurId(10L)).thenReturn(List.of(membre));

        AffectationEquipeChantier aff = new AffectationEquipeChantier();
        aff.setEquipe(equipe);
        aff.setChantier(chantier(5L));
        when(affectationEquipeChantierRepository.findByChantierId(5L)).thenReturn(List.of(aff));

        int creees = service.importerDansChantier(1L, 5L);

        assertThat(creees).isEqualTo(1);
        verify(tacheRepository).save(any(Tache.class));
    }

    // ------------------------------------------------------------------
    // K. transactional
    // ------------------------------------------------------------------

    @Test
    void importerDansChantier_estTransactionnel() throws NoSuchMethodException {
        var method = TemplateTacheServiceImpl.class.getMethod("importerDansChantier", Long.class, Long.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    // ------------------------------------------------------------------
    // L. Taches structurees d'un TemplateTache (LOOP §1, §2, §18)
    // ------------------------------------------------------------------

    @Test
    void ajouterTache_ajouteLaTache() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        CreateTemplateTacheTacheRequest req = new CreateTemplateTacheTacheRequest();
        req.setTitre("Preparer coffrage");
        req.setPriorite(Priorite.MOYENNE);

        service.ajouterTache(1L, req);

        verify(templateTacheTacheRepository).save(argThat((TemplateTacheTache t) ->
                t.getTitre().equals("Preparer coffrage")));
    }

    @Test
    void ajouterTache_titreDuplique_refuse() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(templateTacheTacheRepository.existsByTemplateTacheIdAndTitreIgnoreCase(1L, "COFFRAGE"))
                .thenReturn(true);

        CreateTemplateTacheTacheRequest req = new CreateTemplateTacheTacheRequest();
        req.setTitre("coffrage");

        assertThatThrownBy(() -> service.ajouterTache(1L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listerTaches_renvoieLesTaches() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        TemplateTacheTache item = new TemplateTacheTache();
        item.setId(10L);
        item.setTitre("Couler beton");
        item.setPriorite(Priorite.HAUTE);
        when(templateTacheTacheRepository.findByTemplateTacheIdOrderByIdAsc(1L)).thenReturn(List.of(item));

        var result = service.listerTaches(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("Couler beton");
    }

    @Test
    void retirerTache_supprimeLaBonneEntree() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        TemplateTacheTache item = new TemplateTacheTache();
        TemplateTache parent = templateTache(1L, "Dalle beton");
        item.setId(10L);
        item.setTemplateTache(parent);
        when(templateTacheTacheRepository.findById(10L)).thenReturn(Optional.of(item));

        service.retirerTache(1L, 10L);

        verify(templateTacheTacheRepository).delete(item);
    }

    @Test
    void retirerTache_itemDunAutreTemplate_refuse() {
        TemplateTacheTache item = new TemplateTacheTache();
        item.setId(10L);
        item.setTemplateTache(templateTache(99L, "Autre"));
        when(templateTacheTacheRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.retirerTache(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void modifierTache_metAJourLesChamps() {
        TemplateTache parent = templateTache(1L, "Dalle beton");
        TemplateTacheTache item = new TemplateTacheTache();
        item.setId(10L);
        item.setTemplateTache(parent);
        item.setTitre("Ancien titre");
        when(templateTacheTacheRepository.findById(10L)).thenReturn(Optional.of(item));

        UpdateTemplateTacheTacheRequest req = new UpdateTemplateTacheTacheRequest();
        req.setTitre("Nouveau titre");

        service.modifierTache(1L, 10L, req);

        verify(templateTacheTacheRepository).save(argThat((TemplateTacheTache t) ->
                t.getTitre().equals("Nouveau titre")));
    }

    @Test
    void importerTache_copieLesChampsDeLaTacheReelle() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        TemplateTache parent = templateTache(1L, "Dalle beton");
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        Tache tacheSource = new Tache();
        tacheSource.setId(50L);
        tacheSource.setTitre("Poser ferraillage");
        tacheSource.setDescription("Source");
        tacheSource.setPriorite(Priorite.HAUTE);
        when(tacheRepository.findById(50L)).thenReturn(Optional.of(tacheSource));

        com.cms.template.dto.TemplateTacheTacheResponse result = service.importerTache(1L, 50L);

        verify(templateTacheTacheRepository).save(argThat((TemplateTacheTache t) ->
                t.getTitre().equals("Poser ferraillage") && t.getPriorite() == Priorite.HAUTE));
    }

    @Test
    void importerDansChantier_avecSousTaches_copieChaqueSousTache() {
        when(templateTacheRepository.findById(1L)).thenReturn(Optional.of(templateTache(1L, "Dalle beton")));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockAdministrateur(10L);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        TemplateTacheTache s1 = new TemplateTacheTache();
        s1.setTitre("Coffrage");
        s1.setPriorite(Priorite.MOYENNE);
        TemplateTacheTache s2 = new TemplateTacheTache();
        s2.setTitre("Coulage");
        s2.setPriorite(Priorite.HAUTE);
        when(templateTacheTacheRepository.findByTemplateTacheIdOrderByIdAsc(1L))
                .thenReturn(List.of(s1, s2));

        int creees = service.importerDansChantier(1L, 5L);

        assertThat(creees).isEqualTo(2);
        verify(tacheRepository, org.mockito.Mockito.times(2)).save(any(Tache.class));
    }

}

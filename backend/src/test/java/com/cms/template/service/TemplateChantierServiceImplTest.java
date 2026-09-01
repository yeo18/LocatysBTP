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
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.repository.AffectationTacheRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.TemplateChantierTache;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.mapper.TemplateChantierMapper;
import com.cms.template.mapper.TemplateChantierMapperImpl;
import com.cms.template.mapper.TemplateTacheMapper;
import com.cms.template.mapper.TemplateTacheMapperImpl;
import com.cms.template.repository.TemplateChantierRepository;
import com.cms.template.repository.TemplateChantierTacheRepository;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.repository.TemplateTacheTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service TemplateChantier (repository mocke, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class TemplateChantierServiceImplTest {

    @Mock
    private TemplateChantierRepository templateChantierRepository;
    @Mock
    private TemplateTacheRepository templateTacheRepository;
    @Mock
    private TemplateChantierTacheRepository templateChantierTacheRepository;
    @Mock
    private TemplateTacheTacheRepository templateTacheTacheRepository;
    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private AffectationTacheRepository affectationTacheRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private MembreEquipeRepository membreEquipeRepository;
    @Mock
    private AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    @Mock
    private CurrentUserService currentUserService;

    private TemplateChantierMapper templateChantierMapper = new TemplateChantierMapperImpl();
    private TemplateTacheMapper templateTacheMapper = new TemplateTacheMapperImpl();
    private TemplateChantierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TemplateChantierServiceImpl(templateChantierRepository, templateTacheRepository,
                templateChantierTacheRepository, templateTacheTacheRepository, chantierRepository, tacheRepository,
                affectationTacheRepository,
                utilisateurRepository, membreEquipeRepository,
                affectationEquipeChantierRepository, currentUserService,
                templateChantierMapper, templateTacheMapper);
    }

    private TemplateChantier templateChantier(Long id, String nom, TemplateChantierStatut statut) {
        TemplateChantier template = new TemplateChantier();
        template.setId(id);
        template.setNom(nom);
        template.setTypeConstruction(TypeConstruction.MAISON_R1);
        template.setStatut(statut);
        template.setDateCreation(LocalDateTime.now());
        template.setDateModification(LocalDateTime.now());
        return template;
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

    private TemplateChantierTache association(TemplateChantier template, TemplateTache tache) {
        TemplateChantierTache assoc = new TemplateChantierTache();
        assoc.setTemplateChantier(template);
        assoc.setTemplateTache(tache);
        return assoc;
    }

    private CreateTemplateChantierRequest creerRequest(String nom) {
        CreateTemplateChantierRequest request = new CreateTemplateChantierRequest();
        request.setNom(nom);
        request.setTypeConstruction(TypeConstruction.MAISON_R1);
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
    // A. creer
    // ------------------------------------------------------------------

    @Test
    void creer_fonctionne_avecStatutActif() {
        when(templateChantierRepository.existsByNom("Maison R+1")).thenReturn(false);
        when(templateChantierRepository.save(any(TemplateChantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        TemplateChantierResponse response = service.creer(creerRequest("Maison R+1"));

        assertThat(response.getNom()).isEqualTo("Maison R+1");
        assertThat(response.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
        assertThat(response.getTypeConstruction()).isEqualTo(TypeConstruction.MAISON_R1);
        verify(templateChantierRepository).save(any(TemplateChantier.class));
    }

    @Test
    void creer_nomDejaUtilise_leveConflit() {
        when(templateChantierRepository.existsByNom("Maison R+1")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(creerRequest("Maison R+1")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ------------------------------------------------------------------
    // B. modifier
    // ------------------------------------------------------------------

    @Test
    void modifier_fonctionne_forceStatutActif() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.INACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateChantierRepository.existsByNom("Villa")).thenReturn(false);
        when(templateChantierRepository.save(any(TemplateChantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Villa");
        request.setTypeConstruction(TypeConstruction.VILLA);

        TemplateChantierResponse response = service.modifier(1L, request);

        assertThat(response.getNom()).isEqualTo("Villa");
        assertThat(response.getStatut()).isEqualTo(TemplateChantierStatut.ACTIF);
    }

    @Test
    void modifier_nomDejaUtilise_leveConflit() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateChantierRepository.existsByNom("Immeuble R+5")).thenReturn(true);

        UpdateTemplateChantierRequest request = new UpdateTemplateChantierRequest();
        request.setNom("Immeuble R+5");
        request.setTypeConstruction(TypeConstruction.IMMEUBLE_R5);

        assertThatThrownBy(() -> service.modifier(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void modifier_inexistant_leveException() {
        when(templateChantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(999L, new UpdateTemplateChantierRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // C. desactiver
    // ------------------------------------------------------------------

    @Test
    void desactiver_passeStatutInactif() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateChantierRepository.save(any(TemplateChantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TemplateChantierResponse response = service.desactiver(1L);

        assertThat(response.getStatut()).isEqualTo(TemplateChantierStatut.INACTIF);
    }

    @Test
    void desactiver_inexistant_leveException() {
        when(templateChantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desactiver(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // trouverParId / lister
    // ------------------------------------------------------------------

    @Test
    void trouverParId_fonctionne() {
        when(templateChantierRepository.findById(1L)).thenReturn(
                Optional.of(templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF)));

        TemplateChantierResponse response = service.trouverParId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNom()).isEqualTo("Maison R+1");
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(templateChantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lister_retourneTous() {
        when(templateChantierRepository.findAll()).thenReturn(List.of(
                templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF),
                templateChantier(2L, "Villa", TemplateChantierStatut.INACTIF)));

        assertThat(service.lister()).hasSize(2);
    }

    // ------------------------------------------------------------------
    // association TemplateChantier <-> TemplateTache
    // ------------------------------------------------------------------

    @Test
    void associerTemplateTache_fonctionne() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache tache = templateTache(2L, "Dalle beton");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateTacheRepository.findById(2L)).thenReturn(Optional.of(tache));
        when(templateChantierTacheRepository.existsByTemplateChantierIdAndTemplateTacheId(1L, 2L))
                .thenReturn(false);

        service.associerTemplateTache(1L, 2L);

        verify(templateChantierTacheRepository).save(any(TemplateChantierTache.class));
    }

    @Test
    void associerTemplateTache_dejaAssociee_leveConflit() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache tache = templateTache(2L, "Dalle beton");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateTacheRepository.findById(2L)).thenReturn(Optional.of(tache));
        when(templateChantierTacheRepository.existsByTemplateChantierIdAndTemplateTacheId(1L, 2L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.associerTemplateTache(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void retirerTemplateTache_fonctionne() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache tache = templateTache(2L, "Dalle beton");
        TemplateChantierTache assoc = association(template, tache);
        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of(assoc));

        service.retirerTemplateTache(1L, 2L);

        verify(templateChantierTacheRepository).delete(assoc);
    }

    @Test
    void retirerTemplateTache_inexistante_leveException() {
        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.retirerTemplateTache(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // F. importerDansChantier
    // ------------------------------------------------------------------

    @Test
    void importerDansChantier_copieChaqueTacheSnapshot() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache dalle = templateTache(2L, "Dalle beton");
        TemplateTache toiture = templateTache(3L, "Toiture tole");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockAdministrateur(10L);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());
        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of(association(template, dalle),
                        association(template, toiture)));

        int creees = service.importerDansChantier(1L, 5L);

        assertThat(creees).isEqualTo(2);
        org.mockito.Mockito.verify(tacheRepository, org.mockito.Mockito.times(2)).save(any(Tache.class));
    }

    @Test
    void importerDansChantier_copieSansDuree() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache dalle = templateTache(2L, "Dalle beton");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockAdministrateur(10L);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());
        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of(association(template, dalle)));

        service.importerDansChantier(1L, 5L);

        verify(tacheRepository).save(any(Tache.class));
    }

    @Test
    void importerDansChantier_templateInactif_refuse() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.INACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> service.importerDansChantier(1L, 5L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void importerDansChantier_templateInexistant_leveException() {
        when(templateChantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importerDansChantier(999L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void importerDansChantier_chantierInexistant_leveException() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importerDansChantier(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void importerDansChantier_sansAccesAuChantier_refuse() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockUtilisateur(10L, "UTILISATEUR_STANDARD");
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(membreEquipeRepository.findByUtilisateurId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.importerDansChantier(1L, 5L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void importerDansChantier_membreEquipeAffectee_autorise() {
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache dalle = templateTache(2L, "Dalle beton");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
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

        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of(association(template, dalle)));

        int creees = service.importerDansChantier(1L, 5L);

        assertThat(creees).isEqualTo(1);
        verify(tacheRepository).save(any(Tache.class));
    }

    // ------------------------------------------------------------------
    // K. transactional + L. independance
    // ------------------------------------------------------------------

    @Test
    void importerDansChantier_estTransactionnel() throws NoSuchMethodException {
        var method = TemplateChantierServiceImpl.class.getMethod("importerDansChantier", Long.class, Long.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void independance_apresImport_suppressionTemplateNaffectePasLesTaches() {
        // Un template desactive n'est pas importable, mais les taches deja creees
        // ne referencent pas le template (copie snapshot).
        TemplateChantier template = templateChantier(1L, "Maison R+1", TemplateChantierStatut.ACTIF);
        TemplateTache dalle = templateTache(2L, "Dalle beton");
        when(templateChantierRepository.findById(1L)).thenReturn(Optional.of(template));
        when(chantierRepository.findById(5L)).thenReturn(Optional.of(chantier(5L)));
        mockAdministrateur(10L);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());
        when(templateChantierTacheRepository.findByTemplateChantierIdOrderByIdAsc(1L))
                .thenReturn(List.of(association(template, dalle)));

        service.importerDansChantier(1L, 5L);

        verify(tacheRepository).save(any(Tache.class));
    }

}

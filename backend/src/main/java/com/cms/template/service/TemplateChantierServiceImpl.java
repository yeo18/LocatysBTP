package com.cms.template.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.constants.SystemRoles;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.AffectationTache;
import com.cms.tache.entity.enums.AffectationTacheRole;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.repository.AffectationTacheRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.ImportTemplateChantierAffectationRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.TemplateChantier;
import com.cms.template.entity.enums.TemplateChantierStatut;
import com.cms.template.entity.TemplateChantierTache;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.TemplateTacheTache;
import com.cms.template.mapper.TemplateChantierMapper;
import com.cms.template.mapper.TemplateTacheMapper;
import com.cms.template.repository.TemplateChantierRepository;
import com.cms.template.repository.TemplateChantierTacheRepository;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.repository.TemplateTacheTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service metier TemplateChantier (LOOP 5.8).
 *
 * <p>L'import d'un template dans un chantier est une COPIE (snapshot) :
 * les taches creees n'ont aucune relation avec le template. Securite par
 * donnees (Niveau 2) : un import n'est possible que si l'utilisateur est
 * administrateur ou qu'une de ses equipes est affectee au chantier cible.
 */
@Service
public class TemplateChantierServiceImpl implements TemplateChantierService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateChantierServiceImpl.class);

    private final TemplateChantierRepository templateChantierRepository;
    private final TemplateTacheRepository templateTacheRepository;
    private final TemplateChantierTacheRepository templateChantierTacheRepository;
    private final TemplateTacheTacheRepository templateTacheTacheRepository;
    private final ChantierRepository chantierRepository;
    private final TacheRepository tacheRepository;
    private final AffectationTacheRepository affectationTacheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MembreEquipeRepository membreEquipeRepository;
    private final AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    private final CurrentUserService currentUserService;
    private final TemplateChantierMapper templateChantierMapper;
    private final TemplateTacheMapper templateTacheMapper;

    public TemplateChantierServiceImpl(TemplateChantierRepository templateChantierRepository,
                                       TemplateTacheRepository templateTacheRepository,
                                       TemplateChantierTacheRepository templateChantierTacheRepository,
                                       TemplateTacheTacheRepository templateTacheTacheRepository,
                                       ChantierRepository chantierRepository,
                                       TacheRepository tacheRepository,
                                       AffectationTacheRepository affectationTacheRepository,
                                       UtilisateurRepository utilisateurRepository,
                                       MembreEquipeRepository membreEquipeRepository,
                                       AffectationEquipeChantierRepository affectationEquipeChantierRepository,
                                       CurrentUserService currentUserService,
                                       TemplateChantierMapper templateChantierMapper,
                                       TemplateTacheMapper templateTacheMapper) {
        this.templateChantierRepository = templateChantierRepository;
        this.templateTacheRepository = templateTacheRepository;
        this.templateChantierTacheRepository = templateChantierTacheRepository;
        this.templateTacheTacheRepository = templateTacheTacheRepository;
        this.chantierRepository = chantierRepository;
        this.tacheRepository = tacheRepository;
        this.affectationTacheRepository = affectationTacheRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.membreEquipeRepository = membreEquipeRepository;
        this.affectationEquipeChantierRepository = affectationEquipeChantierRepository;
        this.currentUserService = currentUserService;
        this.templateChantierMapper = templateChantierMapper;
        this.templateTacheMapper = templateTacheMapper;
    }

    @Override
    @Transactional
    public TemplateChantierResponse creer(CreateTemplateChantierRequest request) {
        verifierNomDisponible(request.getNom());

        TemplateChantier templateChantier = new TemplateChantier();
        templateChantier.setNom(request.getNom());
        templateChantier.setDescription(request.getDescription());
        templateChantier.setTypeConstruction(request.getTypeConstruction());
        templateChantier.setDureeEstimeeJours(request.getDureeEstimeeJours());
        templateChantier.setStatut(TemplateChantierStatut.ACTIF);
        templateChantier.setCreatedBy(currentUserService.getCurrentUtilisateur());
        templateChantier.setDateCreation(LocalDateTime.now());
        templateChantier.setDateModification(LocalDateTime.now());

        templateChantier = templateChantierRepository.save(templateChantier);

        logger.info("Creation TemplateChantier : id={}, nom={}", templateChantier.getId(), templateChantier.getNom());
        return toResponse(templateChantier);
    }

    @Override
    @Transactional
    public TemplateChantierResponse modifier(Long id, UpdateTemplateChantierRequest request) {
        TemplateChantier templateChantier = trouverEntityParId(id);

        if (!templateChantier.getNom().equals(request.getNom())) {
            verifierNomDisponible(request.getNom());
        }
        templateChantier.setNom(request.getNom());
        templateChantier.setDescription(request.getDescription());
        templateChantier.setTypeConstruction(request.getTypeConstruction());
        templateChantier.setDureeEstimeeJours(request.getDureeEstimeeJours());
        templateChantier.setStatut(TemplateChantierStatut.ACTIF);
        templateChantier.setDateModification(LocalDateTime.now());

        templateChantier = templateChantierRepository.save(templateChantier);

        logger.info("Modification TemplateChantier : id={}, statut={}", templateChantier.getId(), templateChantier.getStatut());
        return toResponse(templateChantier);
    }

    @Override
    @Transactional
    public TemplateChantierResponse desactiver(Long id) {
        TemplateChantier templateChantier = trouverEntityParId(id);
        templateChantier.setStatut(TemplateChantierStatut.INACTIF);
        templateChantier.setDateModification(LocalDateTime.now());
        templateChantier = templateChantierRepository.save(templateChantier);

        logger.info("Desactivation TemplateChantier : id={}", templateChantier.getId());
        return toResponse(templateChantier);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateChantierResponse trouverParId(Long id) {
        return toResponse(trouverEntityParId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateChantierResponse> lister() {
        return templateChantierRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateChantierResponse> listerParNom(String nom) {
        if (nom == null || nom.isBlank()) {
            return lister();
        }
        return templateChantierRepository.findByNomContainingIgnoreCase(nom.trim()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void associerTemplateTache(Long templateChantierId, Long templateTacheId) {
        TemplateChantier templateChantier = trouverEntityParId(templateChantierId);
        TemplateTache templateTache = templateTacheRepository.findById(templateTacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TemplateTache introuvable avec l'identifiant : " + templateTacheId));

        if (templateChantierTacheRepository.existsByTemplateChantierIdAndTemplateTacheId(
                templateChantierId, templateTacheId)) {
            throw DuplicateResourceException.pourConflit("TemplateTache",
                    "deja associe au TemplateChantier " + templateChantierId);
        }

        TemplateChantierTache association = new TemplateChantierTache();
        association.setTemplateChantier(templateChantier);
        association.setTemplateTache(templateTache);
        templateChantierTacheRepository.save(association);

        logger.info("Association TemplateTache id={} -> TemplateChantier id={}",
                templateTacheId, templateChantierId);
    }

    @Override
    @Transactional
    public void retirerTemplateTache(Long templateChantierId, Long templateTacheId) {
        TemplateChantierTache association = templateChantierTacheRepository
                .findByTemplateChantierIdOrderByIdAsc(templateChantierId).stream()
                .filter(a -> templateTacheId.equals(a.getTemplateTache().getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Association introuvable entre TemplateChantier " + templateChantierId
                                + " et TemplateTache " + templateTacheId));
        templateChantierTacheRepository.delete(association);

        logger.info("Retrait TemplateTache id={} du TemplateChantier id={}",
                templateTacheId, templateChantierId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateTacheResumeResponse> trouverTachesAssociees(Long templateChantierId) {
        trouverEntityParId(templateChantierId);
        return templateChantierTacheRepository
                .findByTemplateChantierIdOrderByIdAsc(templateChantierId).stream()
                .map(TemplateChantierTache::getTemplateTache)
                .map(templateTacheMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int importerDansChantier(Long templateChantierId, Long chantierId) {
        return importerDansChantier(templateChantierId, chantierId, List.of());
    }

    @Override
    @Transactional
    public int importerDansChantier(Long templateChantierId, Long chantierId,
                                    List<ImportTemplateChantierAffectationRequest> affectations) {
        TemplateChantier templateChantier = trouverEntityParId(templateChantierId);
        if (TemplateChantierStatut.INACTIF.equals(templateChantier.getStatut())) {
            throw new BadRequestException(
                    "Impossible d'importer un TemplateChantier desactive : " + templateChantier.getNom());
        }

        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + chantierId));
        verifierAccesChantier(chantier);

        Utilisateur utilisateur = currentUserService.getCurrentUtilisateur();
        List<TemplateChantierTache> associations = templateChantierTacheRepository
                .findByTemplateChantierIdOrderByIdAsc(templateChantierId);

        List<ImportTemplateChantierAffectationRequest> affectationsSafe =
                affectations != null ? affectations : List.of();

        int creees = 0;
        for (TemplateChantierTache association : associations) {
            TemplateTache templateTache = association.getTemplateTache();

            Tache parent = copierVersTache(templateTache, chantier, utilisateur);
            parent = tacheRepository.save(parent);
            attribuerSiConfiguree(parent, templateTache.getId(), null, affectationsSafe);
            creees++;

            List<TemplateTacheTache> sousTaches = templateTacheTacheRepository
                    .findByTemplateTacheIdOrderByIdAsc(templateTache.getId());
            for (TemplateTacheTache sous : sousTaches) {
                Tache tache = copierSousTache(sous, chantier, utilisateur);
                tache = tacheRepository.save(tache);
                attribuerSiConfiguree(tache, templateTache.getId(), sous.getId(), affectationsSafe);
                creees++;
            }
        }

        logger.info("Import TemplateChantier id={} dans chantier id={} : {} tache(s) creee(s) (parents + sous-taches), {} affectation(s)",
                templateChantierId, chantierId, creees, affectationsSafe.size());
        return creees;
    }

    private void attribuerSiConfiguree(Tache tache, Long templateTacheId, Long templateTacheTacheId,
                                       List<ImportTemplateChantierAffectationRequest> affectations) {
        ImportTemplateChantierAffectationRequest affectation = affectations.stream()
                .filter(a -> templateTacheTacheId != null
                        ? templateTacheTacheId.equals(a.getTemplateTacheTacheId())
                        : templateTacheId.equals(a.getTemplateTacheId()) && a.getTemplateTacheTacheId() == null)
                .findFirst()
                .orElse(null);
        if (affectation == null) {
            return;
        }

        Utilisateur cible = utilisateurRepository.findById(affectation.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'identifiant : " + affectation.getUtilisateurId()));

        AffectationTache nouvelle = new AffectationTache();
        nouvelle.setTache(tache);
        nouvelle.setUtilisateur(cible);
        nouvelle.setRole(affectation.getRole());
        nouvelle.setDateAffectation(LocalDate.now());
        affectationTacheRepository.save(nouvelle);

        logger.info("Affectation creee : tache id={}, utilisateur id={}, role={}",
                tache.getId(), cible.getId(), affectation.getRole());
    }

    private Tache copierSousTache(TemplateTacheTache sous, Chantier chantier, Utilisateur utilisateur) {
        Tache tache = new Tache();
        tache.setTitre(sous.getTitre());
        tache.setDescription(sous.getDescription());
        tache.setPriorite(sous.getPriorite());
        tache.setStatus(TacheStatus.A_FAIRE);
        tache.setProgression(0);
        tache.setChantier(chantier);
        tache.setCreatedBy(utilisateur);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        return tache;
    }

    private Tache copierVersTache(TemplateTache templateTache, Chantier chantier, Utilisateur utilisateur) {
        Tache tache = new Tache();
        tache.setTitre(templateTache.getTitre());
        tache.setDescription(templateTache.getDescription());
        tache.setPriorite(templateTache.getPriorite());
        tache.setStatus(TacheStatus.A_FAIRE);
        tache.setProgression(0);
        tache.setChantier(chantier);
        tache.setCreatedBy(utilisateur);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        return tache;
    }

    private void verifierNomDisponible(String nom) {
        if (templateChantierRepository.existsByNom(nom)) {
            throw DuplicateResourceException.pourConflit("nom", nom);
        }
    }

    private TemplateChantier trouverEntityParId(Long id) {
        return templateChantierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TemplateChantier introuvable avec l'identifiant : " + id));
    }

    // ------------------------------------------------------------------
    // Securite par donnees (Niveau 2)
    // ------------------------------------------------------------------

    private void verifierAccesChantier(Chantier chantier) {
        if (!accesChantierAutorise(chantier)) {
            throw ForbiddenException.generic();
        }
    }

    private boolean accesChantierAutorise(Chantier chantier) {
        Long utilisateurId = currentUserService.getCurrentUserId();
        if (estAdministrateur(utilisateurId)) {
            return true;
        }
        Set<Long> equipeIds = equipesDeUtilisateur(utilisateurId);
        if (equipeIds.isEmpty()) {
            return false;
        }
        return affectationEquipeChantierRepository.findByChantierId(chantier.getId()).stream()
                .filter(a -> a.getEquipe() != null)
                .anyMatch(a -> equipeIds.contains(a.getEquipe().getId()));
    }

    private Set<Long> equipesDeUtilisateur(Long utilisateurId) {
        return membreEquipeRepository.findByUtilisateurId(utilisateurId).stream()
                .map(MembreEquipe::getEquipe)
                .filter(e -> e != null)
                .map(e -> e.getId())
                .collect(Collectors.toSet());
    }

    private boolean estAdministrateur(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> ResourceNotFoundException.generic());
        Profil profil = utilisateur.getProfil();
        return profil != null && SystemRoles.ADMINISTRATEUR.equals(profil.getNom());
    }

    private TemplateChantierResponse toResponse(TemplateChantier entity) {
        return templateChantierMapper.toResponse(entity);
    }
}

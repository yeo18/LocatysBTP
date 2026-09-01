package com.cms.chantier.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.ChantierResumeResponse;
import com.cms.chantier.dto.ConfirmerLocalisationRequest;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.dto.UpdateChantierRequest;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.mapper.ChantierMapper;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service metier du module chantier.
 */
@Service
public class ChantierServiceImpl implements ChantierService {

    private static final Logger logger = LoggerFactory.getLogger(ChantierServiceImpl.class);

    private final ChantierRepository chantierRepository;
    private final ChantierMapper chantierMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final DataAccessService dataAccessService;

    public ChantierServiceImpl(ChantierRepository chantierRepository,
                               ChantierMapper chantierMapper,
                               UtilisateurRepository utilisateurRepository,
                               DataAccessService dataAccessService) {
        this.chantierRepository = chantierRepository;
        this.chantierMapper = chantierMapper;
        this.utilisateurRepository = utilisateurRepository;
        this.dataAccessService = dataAccessService;
    }

    @Override
    @Transactional
    public ChantierResponse creer(CreateChantierRequest request) {
        Chantier chantier = chantierMapper.toEntity(request);
        chantier.setResponsable(resoudreResponsable(request.getResponsableId()));
        chantier.setProgression(0);
        chantier.setDateCreation(LocalDateTime.now());
        chantier.setDateModification(LocalDateTime.now());

        chantier = chantierRepository.save(chantier);

        logger.info("Creation chantier : id={}, nom={}, statut={}",
                chantier.getId(), chantier.getNom(), chantier.getStatut());
        return chantierMapper.toResponse(chantier);
    }

    @Override
    @Transactional
    public ChantierResponse modifier(Long id, UpdateChantierRequest request) {
        Chantier chantier = trouverEntityParId(id);

        chantierMapper.update(chantier, request);
        chantier.setResponsable(resoudreResponsable(request.getResponsableId()));
        chantier.setDateModification(LocalDateTime.now());

        chantier = chantierRepository.save(chantier);

        logger.info("Modification chantier : id={}, nom={}", chantier.getId(), chantier.getNom());
        return chantierMapper.toResponse(chantier);
    }

    @Override
    @Transactional
    public ChantierResponse annuler(Long id) {
        Chantier chantier = trouverEntityParId(id);
        if (chantier.getStatut() == ChantierStatut.ANNULE) {
            throw new ResourceNotFoundException("Chantier deja annule : " + id);
        }
        chantier.setStatut(ChantierStatut.ANNULE);
        chantier.setDateModification(LocalDateTime.now());

        chantier = chantierRepository.save(chantier);

        logger.info("Annulation chantier : id={}, nom={}", chantier.getId(), chantier.getNom());
        return chantierMapper.toResponse(chantier);
    }

    @Override
    @Transactional(readOnly = true)
    public ChantierResponse trouverParId(Long id) {
        Chantier chantier = trouverEntityParId(id);
        dataAccessService.verifierAccesChantier(id);
        recalculerProgression(chantier);
        return chantierMapper.toResponse(chantier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChantierResumeResponse> rechercher(SearchRequest search, ChantierStatut statut) {
        Page<Chantier> page = rechercherEntites(search, statut);
        return page.map(chantier -> {
            recalculerProgression(chantier);
            return chantierMapper.toResumeResponse(chantier);
        });
    }

    @Override
    @Transactional
    public ChantierResponse confirmerLocalisation(Long id, ConfirmerLocalisationRequest request) {
        Chantier chantier = trouverEntityParId(id);
        dataAccessService.verifierAccesChantier(id);

        chantier.setLatitude(request.getLatitude());
        chantier.setLongitude(request.getLongitude());
        chantier.setAdresseGeocodee(request.getAdresseGeocodee());
        chantier.setOrigineCoordonnees(request.getOrigineCoordonnees());
        chantier.setFiabiliteCoordonnees(request.getFiabiliteCoordonnees());
        chantier.setDateModification(LocalDateTime.now());

        chantier = chantierRepository.save(chantier);

        logger.info("Confirmation localisation chantier : id={}, lat={}, lon={}, source={}",
                chantier.getId(), chantier.getLatitude(), chantier.getLongitude(),
                chantier.getOrigineCoordonnees());
        return chantierMapper.toResponse(chantier);
    }

    // ---------------------------------------------------------------------
    // Methodes privees
    // ---------------------------------------------------------------------

    private Chantier trouverEntityParId(Long id) {
        return chantierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + id));
    }

    /**
     * Resout le responsable d'un chantier. Absent (null) si aucun identifiant
     * n'est fourni ; sinon verifie l'existence de l'utilisateur.
     */
    private Utilisateur resoudreResponsable(Long responsableId) {
        if (responsableId == null) {
            return null;
        }
        return utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Responsable introuvable avec l'identifiant : " + responsableId));
    }

    /**
     * Progression calculee depuis les taches du chantier : part des taches
     * validees. Aucune valeur saisie manuellement.
     */
    private void recalculerProgression(Chantier chantier) {
        List<Tache> taches = chantier.getTaches();
        if (taches.isEmpty()) {
            chantier.setProgression(0);
            return;
        }
        long terminees = taches.stream()
                .filter(t -> t.getStatus() == TacheStatus.VALIDE)
                .count();
        chantier.setProgression((int) Math.round(terminees * 100.0 / taches.size()));
    }

    private Page<Chantier> rechercherEntites(SearchRequest search, ChantierStatut statut) {
        Pageable pageable = construirePageable(search);

        List<Chantier> source;
        if (statut != null) {
            source = chantierRepository.findByStatut(statut, Pageable.unpaged()).getContent();
        } else {
            source = chantierRepository.findAll();
        }

        // SÃ©curitÃ© par donnÃ©es (Niveau 2) : un utilisateur non administrateur
        // ne voit que les chantiers oÃ¹ l'une de ses Ã©quipes est affectÃ©e.
        if (!dataAccessService.estAdministrateur()) {
            Set<Long> chantiersAccessibles = dataAccessService.chantiersDeUtilisateur();
            source = source.stream()
                    .filter(c -> chantiersAccessibles.contains(c.getId()))
                    .collect(Collectors.toList());
        }

        String motCle = search.getMotCle();
        if (motCle != null && !motCle.isBlank()) {
            String mc = motCle.toLowerCase().trim();
            source = source.stream()
                    .filter(c -> c.getNom().toLowerCase().contains(mc)
                            || (c.getAdresseSaisie() != null && c.getAdresseSaisie().toLowerCase().contains(mc)))
                    .collect(Collectors.toList());
        }

        int debut = Math.min((int) pageable.getOffset(), source.size());
        int fin = Math.min(debut + pageable.getPageSize(), source.size());
        List<Chantier> contenu = source.subList(debut, fin);

        return new PageImpl<>(contenu, pageable, source.size());
    }

    private Pageable construirePageable(SearchRequest search) {
        if (search.getSort() == null || search.getSort().isBlank()) {
            return PageRequest.of(search.getPage(), search.getSize());
        }
        Sort.Direction direction = "DESC".equalsIgnoreCase(search.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(search.getPage(), search.getSize(), Sort.by(direction, search.getSort()));
    }

}

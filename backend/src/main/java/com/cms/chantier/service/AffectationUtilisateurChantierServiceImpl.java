package com.cms.chantier.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.dto.AffectationUtilisateurChantierResponse;
import com.cms.chantier.dto.CreateAffectationUtilisateurChantierRequest;
import com.cms.chantier.dto.ModifierAffectationUtilisateurChantierRequest;
import com.cms.chantier.entity.AffectationUtilisateurChantier;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationUtilisateurChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implémentation du service des affectations directes utilisateur <-> chantier
 * (relation ternaire avec profil et période de validité).
 */
@Service
public class AffectationUtilisateurChantierServiceImpl implements AffectationUtilisateurChantierService {

    private static final Logger logger = LoggerFactory.getLogger(AffectationUtilisateurChantierServiceImpl.class);

    private final AffectationUtilisateurChantierRepository affectationRepository;
    private final ChantierRepository chantierRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurMapper utilisateurMapper;

    public AffectationUtilisateurChantierServiceImpl(
            AffectationUtilisateurChantierRepository affectationRepository,
            ChantierRepository chantierRepository,
            UtilisateurRepository utilisateurRepository,
            ProfilRepository profilRepository,
            UtilisateurMapper utilisateurMapper) {
        this.affectationRepository = affectationRepository;
        this.chantierRepository = chantierRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.profilRepository = profilRepository;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    @Transactional
    public AffectationUtilisateurChantierResponse affecter(Long chantierId,
                                                            CreateAffectationUtilisateurChantierRequest request) {
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + chantierId));
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'identifiant : " + request.getUtilisateurId()));

        Profil profil = determinerProfil(utilisateur, request.getProfilId());
        LocalDateTime debut = request.getDateDebut() != null ? request.getDateDebut() : LocalDateTime.now();
        LocalDateTime fin = request.getDateFin();
        verifierPeriode(debut, fin);
        verifierUnicitePeriode(request.getUtilisateurId(), chantierId, null, debut, fin);

        AffectationUtilisateurChantier affectation = new AffectationUtilisateurChantier();
        affectation.setUtilisateur(utilisateur);
        affectation.setChantier(chantier);
        affectation.setProfil(profil);
        affectation.setDateAffectation(LocalDateTime.now());
        affectation.setDateDebut(debut);
        affectation.setDateFin(fin);
        affectation = affectationRepository.save(affectation);

        logger.info("Affectation de l'utilisateur {} au chantier {} avec le profil {}",
                request.getUtilisateurId(), chantierId, profil.getNom());
        return toResponse(affectation);
    }

    @Override
    @Transactional
    public AffectationUtilisateurChantierResponse modifier(Long affectationId,
                                                            ModifierAffectationUtilisateurChantierRequest request) {
        AffectationUtilisateurChantier affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Affectation introuvable avec l'identifiant : " + affectationId));

        LocalDateTime debut = request.getDateDebut() != null ? request.getDateDebut() : affectation.getDateDebut();
        LocalDateTime fin = request.getDateFin() != null ? request.getDateFin() : affectation.getDateFin();
        verifierPeriode(debut, fin);

        if (request.getProfilId() != null) {
            Profil profil = profilRepository.findById(request.getProfilId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Profil introuvable avec l'identifiant : " + request.getProfilId()));
            affectation.setProfil(profil);
        }
        affectation.setDateDebut(debut);
        affectation.setDateFin(fin);

        verifierUnicitePeriode(affectation.getUtilisateur().getId(),
                affectation.getChantier().getId(), affectationId, debut, fin);

        affectation = affectationRepository.save(affectation);
        return toResponse(affectation);
    }

    @Override
    @Transactional
    public void retirer(Long chantierId, Long utilisateurId) {
        List<AffectationUtilisateurChantier> affectations = affectationRepository
                .findByUtilisateurIdAndChantierId(utilisateurId, chantierId);
        if (affectations.isEmpty()) {
            throw new ResourceNotFoundException("Affectation introuvable");
        }
        affectationRepository.deleteAll(affectations);
        logger.info("Retrait de l'utilisateur {} du chantier {}", utilisateurId, chantierId);
    }

    @Override
    @Transactional
    public void supprimer(Long affectationId) {
        AffectationUtilisateurChantier affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Affectation introuvable avec l'identifiant : " + affectationId));
        affectationRepository.delete(affectation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResumeResponse> listerUtilisateursDuChantier(Long chantierId) {
        return affectationRepository.findByChantierId(chantierId).stream()
                .map(a -> utilisateurMapper.toResumeResponse(a.getUtilisateur()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationUtilisateurChantierResponse> listerAffectationsDuChantier(Long chantierId) {
        return affectationRepository.findByChantierId(chantierId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Profil a appliquer : celui demandé, sinon le profil global de l'utilisateur.
     */
    private Profil determinerProfil(Utilisateur utilisateur, Long profilId) {
        if (profilId == null) {
            return utilisateur.getProfil();
        }
        return profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'identifiant : " + profilId));
    }

    private void verifierPeriode(LocalDateTime debut, LocalDateTime fin) {
        if (fin != null && debut.isAfter(fin)) {
            throw new BadRequestException("La date de fin doit être postérieure à la date de début");
        }
    }

    /**
     * Sur un chantier, un utilisateur a UN SEUL profil par période : aucun
     * chevauchement de période pour le même couple (utilisateur, chantier).
     */
    private void verifierUnicitePeriode(Long utilisateurId, Long chantierId,
                                        Long affectationExclueId,
                                        LocalDateTime debut, LocalDateTime fin) {
        boolean chevauchement = affectationRepository
                .findByUtilisateurIdAndChantierId(utilisateurId, chantierId).stream()
                .filter(a -> affectationExclueId == null || !affectationExclueId.equals(a.getId()))
                .anyMatch(a -> chevauche(a.getDateDebut(), a.getDateFin(), debut, fin));
        if (chevauchement) {
            throw new DuplicateResourceException(
                    "Cet utilisateur a déjà un profil sur ce chantier pendant cette période");
        }
    }

    private boolean chevauche(LocalDateTime aDebut, LocalDateTime aFin,
                              LocalDateTime bDebut, LocalDateTime bFin) {
        LocalDateTime aEnd = aFin != null ? aFin : LocalDateTime.MAX;
        LocalDateTime bEnd = bFin != null ? bFin : LocalDateTime.MAX;
        return !aDebut.isAfter(bEnd) && !bDebut.isAfter(aEnd);
    }

    private AffectationUtilisateurChantierResponse toResponse(AffectationUtilisateurChantier a) {
        AffectationUtilisateurChantierResponse response = new AffectationUtilisateurChantierResponse();
        response.setId(a.getId());
        response.setUtilisateurId(a.getUtilisateur().getId());
        response.setUtilisateurNom(a.getUtilisateur().getNom());
        response.setUtilisateurPrenom(a.getUtilisateur().getPrenom());
        response.setChantierId(a.getChantier().getId());
        response.setChantierNom(a.getChantier().getNom());
        response.setProfilId(a.getProfil().getId());
        response.setProfilNom(a.getProfil().getNom());
        response.setDateAffectation(a.getDateAffectation());
        response.setDateDebut(a.getDateDebut());
        response.setDateFin(a.getDateFin());
        return response;
    }
}
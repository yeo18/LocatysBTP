package com.cms.profil.service;

import java.time.LocalDateTime;
import java.util.List;
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

import com.cms.common.constants.AuditEvents;
import com.cms.common.constants.SystemRoles;
import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.NomProfilDejaExistantException;
import com.cms.exception.custom.PermissionDejaAttribueeException;
import com.cms.exception.custom.PermissionIntrouvableException;
import com.cms.exception.custom.ProfilIntrouvableException;
import com.cms.exception.custom.ProfilSystemeProtegeException;
import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilPermissionResponse;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.dto.ProfilResumeResponse;
import com.cms.profil.dto.UpdateProfilRequest;
import com.cms.profil.entity.Profil;
import com.cms.profil.entity.ProfilPermission;
import com.cms.profil.mapper.ProfilMapper;
import com.cms.profil.mapper.ProfilPermissionMapper;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.profil.repository.ProfilRepository;

/**
 * Implementation du service des profils (roles RBAC) et de leurs permissions.
 */
@Service
public class ProfilServiceImpl implements ProfilService {

    private static final Logger logger = LoggerFactory.getLogger(ProfilServiceImpl.class);

    private final ProfilRepository profilRepository;
    private final ProfilPermissionRepository profilPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final ProfilMapper profilMapper;
    private final ProfilPermissionMapper profilPermissionMapper;

    public ProfilServiceImpl(ProfilRepository profilRepository,
                             ProfilPermissionRepository profilPermissionRepository,
                             PermissionRepository permissionRepository,
                             ProfilMapper profilMapper,
                             ProfilPermissionMapper profilPermissionMapper) {
        this.profilRepository = profilRepository;
        this.profilPermissionRepository = profilPermissionRepository;
        this.permissionRepository = permissionRepository;
        this.profilMapper = profilMapper;
        this.profilPermissionMapper = profilPermissionMapper;
    }

    @Override
    @Transactional
    public ProfilResponse creerProfil(CreateProfilRequest request) {
        verifierNomDisponible(request.getNom());

        Profil profil = profilMapper.toEntity(request);
        profil.setDateCreation(LocalDateTime.now());
        profil.setDateModification(LocalDateTime.now());

        profil = profilRepository.save(profil);

        logger.info("AUDIT|{}|Creation profil : id={}, nom={}", AuditEvents.CREATION_PROFIL, profil.getId(), profil.getNom());
        return profilMapper.toResponse(profil);
    }

    @Override
    @Transactional
    public ProfilResponse modifierProfil(Long id, UpdateProfilRequest request) {
        Profil profil = trouverEntityParId(id);

        verifierNomSystemeInchange(profil, request.getNom());

        if (!profil.getNom().equals(request.getNom())) {
            verifierNomDisponible(request.getNom());
        }

        profilMapper.update(profil, request);
        profil.setDateModification(LocalDateTime.now());

        profil = profilRepository.save(profil);

        logger.info("AUDIT|{}|Modification profil : id={}, nom={}",
                AuditEvents.MODIFICATION_PROFIL, profil.getId(), profil.getNom());
        return profilMapper.toResponse(profil);
    }

    @Override
    @Transactional
    public void supprimerProfil(Long id) {
        Profil profil = trouverEntityParId(id);
        verifierNonSysteme(profil, ProfilSystemeProtegeException.nonSupprimable(profil.getNom()));

        if (!profil.getUtilisateurs().isEmpty()) {
            throw new BadRequestException("Le profil est encore attribue a des utilisateurs : " + profil.getNom());
        }

        profilPermissionRepository.deleteAll(profilPermissionRepository.findByProfilId(id));
        profilRepository.delete(profil);

        logger.info("AUDIT|{}|Suppression profil : id={}, nom={}",
                AuditEvents.SUPPRESSION_PROFIL, profil.getId(), profil.getNom());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilResponse trouverParId(Long id) {
        return profilMapper.toResponse(trouverEntityParId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfilResumeResponse> rechercher(SearchRequest search) {
        Pageable pageable = construirePageable(search);

        Page<Profil> page;
        if (search.getMotCle() == null || search.getMotCle().isBlank()) {
            page = profilRepository.findAll(pageable);
        } else {
            page = rechercherParMotCle(search, pageable);
        }
        return page.map(profilMapper::toResumeResponse);
    }

    @Override
    @Transactional
    public ProfilPermissionResponse ajouterPermission(Long profilId, Long permissionId) {
        Profil profil = trouverEntityParId(profilId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> PermissionIntrouvableException.pourId(permissionId));

        if (profilPermissionRepository.findByProfilIdAndPermissionId(profilId, permissionId).isPresent()) {
            throw PermissionDejaAttribueeException.pourProfil(profilId, permissionId);
        }

        ProfilPermission association = new ProfilPermission();
        association.setProfil(profil);
        association.setPermission(permission);
        association = profilPermissionRepository.save(association);

        logger.info("AUDIT|{}|Attribution permission au profil : profilId={}, permissionId={}",
                AuditEvents.ATTRIBUTION_PERMISSION, profilId, permissionId);
        return profilPermissionMapper.toResponse(association);
    }

    @Override
    @Transactional
    public void retirerPermission(Long profilId, Long permissionId) {
        ProfilPermission association = profilPermissionRepository
                .findByProfilIdAndPermissionId(profilId, permissionId)
                .orElseThrow(() -> new BadRequestException(
                        "La permission n'est pas attribuee au profil : profilId=" + profilId
                                + ", permissionId=" + permissionId));
        profilPermissionRepository.delete(association);

        logger.info("AUDIT|{}|Retrait permission du profil : profilId={}, permissionId={}",
                AuditEvents.RETRAIT_PERMISSION, profilId, permissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilPermissionResponse> listerPermissions(Long profilId) {
        trouverEntityParId(profilId);
        return profilPermissionRepository.findByProfilId(profilId).stream()
                .map(profilPermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Methodes privees
    // ---------------------------------------------------------------------

    private Profil trouverEntityParId(Long id) {
        return profilRepository.findById(id)
                .orElseThrow(() -> ProfilIntrouvableException.pourId(id));
    }

    private void verifierNomDisponible(String nom) {
        if (profilRepository.existsByNom(nom)) {
            throw NomProfilDejaExistantException.pourNom(nom);
        }
    }

    private boolean estProfilSysteme(Profil profil) {
        return SystemRoles.ADMINISTRATEUR.equals(profil.getNom())
                || SystemRoles.UTILISATEUR_STANDARD.equals(profil.getNom());
    }

    private void verifierNonSysteme(Profil profil, RuntimeException exception) {
        if (estProfilSysteme(profil)) {
            throw exception;
        }
    }

    private void verifierNomSystemeInchange(Profil profil, String nouveauNom) {
        if (estProfilSysteme(profil) && !profil.getNom().equals(nouveauNom)) {
            throw ProfilSystemeProtegeException.nonRenommable(profil.getNom());
        }
    }

    private Page<Profil> rechercherParMotCle(SearchRequest search, Pageable pageable) {
        String motCle = search.getMotCle().toLowerCase().trim();
        List<Profil> filtrer = profilRepository.findAll().stream()
                .filter(p -> p.getNom().toLowerCase().contains(motCle)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(motCle)))
                .collect(Collectors.toList());

        int debut = Math.min((int) pageable.getOffset(), filtrer.size());
        int fin = Math.min(debut + pageable.getPageSize(), filtrer.size());
        return new PageImpl<>(filtrer.subList(debut, fin), pageable, filtrer.size());
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

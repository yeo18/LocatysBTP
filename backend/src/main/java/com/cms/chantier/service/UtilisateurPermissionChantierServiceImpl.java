package com.cms.chantier.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.dto.UtilisateurPermissionChantierResponse;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.UtilisateurPermissionChantier;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.chantier.repository.UtilisateurPermissionChantierRepository;
import com.cms.common.constants.AuditEvents;
import com.cms.exception.custom.PermissionDejaAttribueeException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service des exceptions RBAC scopÃ©es par chantier.
 */
@Service
public class UtilisateurPermissionChantierServiceImpl implements UtilisateurPermissionChantierService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurPermissionChantierServiceImpl.class);

    private final UtilisateurPermissionChantierRepository repository;
    private final UtilisateurRepository utilisateurRepository;
    private final ChantierRepository chantierRepository;
    private final PermissionRepository permissionRepository;
    private final CurrentUserService currentUserService;

    public UtilisateurPermissionChantierServiceImpl(
            UtilisateurPermissionChantierRepository repository,
            UtilisateurRepository utilisateurRepository,
            ChantierRepository chantierRepository,
            PermissionRepository permissionRepository,
            CurrentUserService currentUserService) {
        this.repository = repository;
        this.utilisateurRepository = utilisateurRepository;
        this.chantierRepository = chantierRepository;
        this.permissionRepository = permissionRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public UtilisateurPermissionChantierResponse accorder(Long chantierId, Long utilisateurId, Long permissionId) {
        return ajouterException(chantierId, utilisateurId, permissionId, UtilisateurPermissionType.ACCORDER);
    }

    @Override
    @Transactional
    public UtilisateurPermissionChantierResponse refuser(Long chantierId, Long utilisateurId, Long permissionId) {
        return ajouterException(chantierId, utilisateurId, permissionId, UtilisateurPermissionType.REFUSER);
    }

    @Override
    @Transactional
    public void retirer(Long exceptionId) {
        UtilisateurPermissionChantier exception = repository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exception scopee de permission introuvable avec l'identifiant : " + exceptionId));
        repository.delete(exception);
        logger.info("AUDIT|{}|Retrait exception RBAC scoped : id={}", AuditEvents.RETRAIT_PERMISSION_INDIVIDUELLE,
                exceptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurPermissionChantierResponse> listerParChantier(Long chantierId) {
        return repository.findByChantierId(chantierId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UtilisateurPermissionChantierResponse ajouterException(Long chantierId, Long utilisateurId,
                                                                  Long permissionId, UtilisateurPermissionType type) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'identifiant : " + utilisateurId));
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + chantierId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission introuvable avec l'identifiant : " + permissionId));

        if (repository.findByUtilisateurIdAndChantierIdAndPermissionIdAndType(
                utilisateurId, chantierId, permissionId, type).isPresent()) {
            throw PermissionDejaAttribueeException.pourUtilisateur(utilisateurId, permissionId,
                    type.name() + " sur le chantier " + chantierId);
        }

        UtilisateurPermissionChantier exception = new UtilisateurPermissionChantier();
        exception.setUtilisateur(utilisateur);
        exception.setChantier(chantier);
        exception.setPermission(permission);
        exception.setType(type);
        exception.setDateCreation(LocalDateTime.now());
        exception.setDateModification(LocalDateTime.now());
        exception.setCreatedBy(currentUserService.getCurrentUtilisateur());
        exception = repository.save(exception);

        logger.info("AUDIT|{}|Exception RBAC scoped : utilisateurId={}, chantierId={}, permissionId={}, type={}",
                type == UtilisateurPermissionType.ACCORDER
                        ? AuditEvents.ACCORD_PERMISSION : AuditEvents.REFUS_PERMISSION,
                utilisateurId, chantierId, permissionId, type);
        return toResponse(exception);
    }

    private UtilisateurPermissionChantierResponse toResponse(UtilisateurPermissionChantier e) {
        UtilisateurPermissionChantierResponse response = new UtilisateurPermissionChantierResponse();
        response.setId(e.getId());
        response.setUtilisateurId(e.getUtilisateur().getId());
        response.setUtilisateurNom(e.getUtilisateur().getNom());
        response.setUtilisateurPrenom(e.getUtilisateur().getPrenom());
        response.setChantierId(e.getChantier().getId());
        response.setChantierNom(e.getChantier().getNom());
        response.setPermissionId(e.getPermission().getId());
        response.setNomPermission(e.getPermission().getNomPermission());
        response.setPermissionNom(e.getPermission().getNom());
        response.setType(e.getType());
        response.setCreatedById(e.getCreatedBy().getId());
        response.setDateCreation(e.getDateCreation());
        response.setDateModification(e.getDateModification());
        return response;
    }
}
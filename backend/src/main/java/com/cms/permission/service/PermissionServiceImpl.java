package com.cms.permission.service;

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
import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.NomPermissionDejaExistantException;
import com.cms.exception.custom.PermissionIntrouvableException;
import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.dto.PermissionResumeResponse;
import com.cms.permission.dto.UpdatePermissionRequest;
import com.cms.permission.entity.Permission;
import com.cms.permission.mapper.PermissionMapper;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;

/**
 * Implementation du service du referentiel des permissions (RBAC).
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionRepository permissionRepository;
    private final ProfilPermissionRepository profilPermissionRepository;
    private final UtilisateurPermissionRepository utilisateurPermissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionRepository permissionRepository,
                                 ProfilPermissionRepository profilPermissionRepository,
                                 UtilisateurPermissionRepository utilisateurPermissionRepository,
                                 PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.profilPermissionRepository = profilPermissionRepository;
        this.utilisateurPermissionRepository = utilisateurPermissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional
    public PermissionResponse creerPermission(CreatePermissionRequest request) {
        verifierNomPermissionDisponible(request.getNomPermission());

        Permission permission = permissionMapper.toEntity(request);
        normaliserModule(permission, request);
        permission = permissionRepository.save(permission);

        logger.info("AUDIT|{}|Creation permission : id={}, nomPermission={}, module={}",
                AuditEvents.CREATION_PERMISSION,
                permission.getId(), permission.getNomPermission(), permission.getModule());
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse modifierPermission(Long id, UpdatePermissionRequest request) {
        Permission permission = trouverEntityParId(id);

        if (!permission.getNomPermission().equals(request.getNomPermission())) {
            verifierNomPermissionDisponible(request.getNomPermission());
        }

        permissionMapper.update(permission, request);
        normaliserModule(permission, request);
        permission = permissionRepository.save(permission);

        logger.info("AUDIT|{}|Modification permission : id={}, nomPermission={}",
                AuditEvents.MODIFICATION_PERMISSION, permission.getId(), permission.getNomPermission());
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public void supprimerPermission(Long id) {
        Permission permission = trouverEntityParId(id);
        retirerTousLesOctrois(id);

        permissionRepository.delete(permission);
        logger.info("AUDIT|{}|Suppression permission : id={}, nomPermission={}",
                AuditEvents.SUPPRESSION_PERMISSION, permission.getId(), permission.getNomPermission());
    }

    @Override
    @Transactional
    public PermissionResponse desactiverPermission(Long id) {
        Permission permission = trouverEntityParId(id);
        retirerTousLesOctrois(id);

        logger.info("AUDIT|{}|Desactivation permission : id={}, nomPermission={} (octrois retires)",
                AuditEvents.DESACTIVATION_PERMISSION, permission.getId(), permission.getNomPermission());
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse activerPermission(Long id) {
        Permission permission = trouverEntityParId(id);

        logger.info("AUDIT|{}|Activation permission : id={}, nomPermission={} (referentiel permanent, re-attribution requise)",
                AuditEvents.ACTIVATION_PERMISSION, permission.getId(), permission.getNomPermission());
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse trouverParId(Long id) {
        return permissionMapper.toResponse(trouverEntityParId(id));
    }

@Override
    @Transactional(readOnly = true)
    public PermissionResponse trouverParNomPermission(String nomPermission) {
        Permission permission = permissionRepository.findByNomPermission(nomPermission)
                .orElseThrow(() -> PermissionIntrouvableException.pourNomPermission(nomPermission));
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResumeResponse> listerParModule(String module) {
        return permissionRepository.findByModule(module.toUpperCase()).stream()
                .map(permissionMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResumeResponse> rechercher(SearchRequest search) {
        Pageable pageable = construirePageable(search);

        Page<Permission> page;
        if (search.getMotCle() == null || search.getMotCle().isBlank()) {
            page = permissionRepository.findAll(pageable);
        } else {
            page = rechercherParMotCle(search, pageable);
        }
        return page.map(permissionMapper::toResumeResponse);
    }

    // ---------------------------------------------------------------------
    // Methodes privees
    // ---------------------------------------------------------------------

    private Permission trouverEntityParId(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> PermissionIntrouvableException.pourId(id));
    }

    private void verifierNomPermissionDisponible(String nomPermission) {
        if (permissionRepository.existsByNomPermission(nomPermission)) {
            throw NomPermissionDejaExistantException.pourNomPermission(nomPermission);
        }
    }

    /**
     * Applique la convention MODULE_ACTION : le module est
     * normalise en MAJUSCULES.
     */
    private void normaliserModule(Permission permission, CreatePermissionRequest request) {
        permission.setModule(request.getModule().toUpperCase());
    }

    private void normaliserModule(Permission permission, UpdatePermissionRequest request) {
        permission.setModule(request.getModule().toUpperCase());
    }

    /**
     * Retire tous les octrois d'une permission (profil_permission et
     * utilisateur_permission). Utilise par supprimer/desactiver.
     */
    private void retirerTousLesOctrois(Long permissionId) {
        profilPermissionRepository.findByPermissionId(permissionId)
                .forEach(profilPermissionRepository::delete);
        utilisateurPermissionRepository.findByPermissionId(permissionId)
                .forEach(utilisateurPermissionRepository::delete);
    }

    private Page<Permission> rechercherParMotCle(SearchRequest search, Pageable pageable) {
        String motCle = search.getMotCle().toLowerCase().trim();
        List<Permission> filtrer = permissionRepository.findAll().stream()
                .filter(p -> p.getNom().toLowerCase().contains(motCle)
                        || p.getNomPermission().toLowerCase().contains(motCle)
                        || p.getModule().toLowerCase().contains(motCle))
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

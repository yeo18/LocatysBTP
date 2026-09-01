package com.cms.utilisateur.mapper;

import com.cms.utilisateur.dto.CreateUtilisateurPermissionRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurPermissionRequest;
import com.cms.utilisateur.dto.UtilisateurPermissionResponse;
import com.cms.utilisateur.dto.UtilisateurPermissionResumeResponse;
import com.cms.utilisateur.entity.UtilisateurPermission;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UtilisateurPermissionMapper {

    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    @Mapping(source = "permission.id", target = "permissionId")
    @Mapping(source = "permission.nomPermission", target = "nomPermission")
    @Mapping(source = "createdBy.id", target = "createdById")
    UtilisateurPermissionResponse toResponse(UtilisateurPermission entity);

    @Mapping(source = "permission.nomPermission", target = "nomPermission")
    UtilisateurPermissionResumeResponse toResumeResponse(UtilisateurPermission entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    UtilisateurPermission toEntity(CreateUtilisateurPermissionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    void update(@MappingTarget UtilisateurPermission entity, UpdateUtilisateurPermissionRequest request);
}

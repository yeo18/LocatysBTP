package com.cms.profil.mapper;

import com.cms.profil.dto.CreateProfilPermissionRequest;
import com.cms.profil.dto.UpdateProfilPermissionRequest;
import com.cms.profil.dto.ProfilPermissionResponse;
import com.cms.profil.dto.ProfilPermissionResumeResponse;
import com.cms.profil.entity.ProfilPermission;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfilPermissionMapper {

    @Mapping(source = "profil.id", target = "profilId")
    @Mapping(source = "profil.nom", target = "profilNom")
    @Mapping(source = "permission.id", target = "permissionId")
    @Mapping(source = "permission.nomPermission", target = "nomPermission")
    @Mapping(source = "permission.nom", target = "permissionNom")
    ProfilPermissionResponse toResponse(ProfilPermission entity);

    @Mapping(source = "permission.nomPermission", target = "nomPermission")
    ProfilPermissionResumeResponse toResumeResponse(ProfilPermission entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "permission", ignore = true)
    ProfilPermission toEntity(CreateProfilPermissionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "permission", ignore = true)
    void update(@MappingTarget ProfilPermission entity, UpdateProfilPermissionRequest request);
}

package com.cms.profil.mapper;

import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.UpdateProfilRequest;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.dto.ProfilResumeResponse;
import com.cms.profil.entity.Profil;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfilMapper {

    ProfilResponse toResponse(Profil entity);

    ProfilResumeResponse toResumeResponse(Profil entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "profilPermissions", ignore = true)
    @Mapping(target = "utilisateurs", ignore = true)
    Profil toEntity(CreateProfilRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "profilPermissions", ignore = true)
    @Mapping(target = "utilisateurs", ignore = true)
    void update(@MappingTarget Profil entity, UpdateProfilRequest request);
}

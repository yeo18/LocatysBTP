package com.cms.tache.mapper;

import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.UpdateValidationTacheRequest;
import com.cms.tache.dto.ValidationTacheResponse;
import com.cms.tache.dto.ValidationTacheResumeResponse;
import com.cms.tache.entity.ValidationTache;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ValidationTacheMapper {

    @Mapping(source = "tache.id", target = "tacheId")
    @Mapping(source = "tache.titre", target = "tacheTitre")
    @Mapping(source = "validateur.id", target = "validateurId")
    @Mapping(source = "validateur.nom", target = "validateurNom")
    @Mapping(source = "validateur.prenom", target = "validateurPrenom")
    ValidationTacheResponse toResponse(ValidationTache entity);

    @Mapping(source = "tache.id", target = "tacheId")
    ValidationTacheResumeResponse toResumeResponse(ValidationTache entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tache", ignore = true)
    @Mapping(target = "validateur", ignore = true)
    ValidationTache toEntity(CreateValidationTacheRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tache", ignore = true)
    @Mapping(target = "validateur", ignore = true)
    void update(@MappingTarget ValidationTache entity, UpdateValidationTacheRequest request);
}

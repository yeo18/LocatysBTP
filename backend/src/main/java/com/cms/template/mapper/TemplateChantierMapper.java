package com.cms.template.mapper;

import com.cms.template.dto.CreateTemplateChantierRequest;
import com.cms.template.dto.TemplateChantierResponse;
import com.cms.template.dto.TemplateChantierResumeResponse;
import com.cms.template.dto.UpdateTemplateChantierRequest;
import com.cms.template.entity.TemplateChantier;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TemplateChantierMapper {

    TemplateChantierResponse toResponse(TemplateChantier entity);

    TemplateChantierResumeResponse toResumeResponse(TemplateChantier entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "taches", ignore = true)
    TemplateChantier toEntity(CreateTemplateChantierRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "taches", ignore = true)
    void update(@MappingTarget TemplateChantier entity, UpdateTemplateChantierRequest request);
}

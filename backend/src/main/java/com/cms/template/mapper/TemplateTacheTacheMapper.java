package com.cms.template.mapper;

import com.cms.template.dto.CreateTemplateTacheTacheRequest;
import com.cms.template.dto.TemplateTacheTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheTacheRequest;
import com.cms.template.entity.TemplateTacheTache;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TemplateTacheTacheMapper {

    @Mapping(target = "templateTacheId", source = "templateTache.id")
    TemplateTacheTacheResponse toResponse(TemplateTacheTache entity);

    TemplateTacheTacheResumeResponse toResumeResponse(TemplateTacheTache entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateTache", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    TemplateTacheTache toEntity(CreateTemplateTacheTacheRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateTache", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void update(@MappingTarget TemplateTacheTache entity, UpdateTemplateTacheTacheRequest request);
}
package com.cms.template.mapper;

import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.TemplateTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.entity.TemplateTache;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TemplateTacheMapper {

    TemplateTacheResponse toResponse(TemplateTache entity);

    TemplateTacheResumeResponse toResumeResponse(TemplateTache entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "templates", ignore = true)
    TemplateTache toEntity(CreateTemplateTacheRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "templates", ignore = true)
    void update(@MappingTarget TemplateTache entity, UpdateTemplateTacheRequest request);
}

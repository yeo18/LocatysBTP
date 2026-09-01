package com.cms.permission.mapper;

import com.cms.permission.dto.CreatePermissionRequest;
import com.cms.permission.dto.UpdatePermissionRequest;
import com.cms.permission.dto.PermissionResponse;
import com.cms.permission.dto.PermissionResumeResponse;
import com.cms.permission.entity.Permission;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission entity);

    PermissionResumeResponse toResumeResponse(Permission entity);

    @Mapping(target = "id", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Permission entity, UpdatePermissionRequest request);
}

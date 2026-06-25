package com.projecthub.mapper;

import com.projecthub.dto.RequirementCreateRequest;
import com.projecthub.dto.RequirementDto;
import com.projecthub.entity.Requirement;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequirementMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    RequirementDto toDto(Requirement requirement);

    List<RequirementDto> toDtoList(List<Requirement> requirements);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Requirement toEntity(RequirementCreateRequest request);
}

package com.projecthub.mapper;

import com.projecthub.dto.SprintCreateRequest;
import com.projecthub.dto.SprintDto;
import com.projecthub.entity.Sprint;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SprintMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "name", source = "sprintName")
    SprintDto toDto(Sprint sprint);

    List<SprintDto> toDtoList(List<Sprint> sprints);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "sprintName", source = "name")
    Sprint toEntity(SprintCreateRequest request);
}

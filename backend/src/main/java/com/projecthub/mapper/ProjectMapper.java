package com.projecthub.mapper;

import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.Project;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {

    ProjectDto toDto(Project project);

    List<ProjectDto> toDtoList(List<Project> projects);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "sprints", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(ProjectCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "sprints", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntity(ProjectUpdateRequest request, @MappingTarget Project project);
}

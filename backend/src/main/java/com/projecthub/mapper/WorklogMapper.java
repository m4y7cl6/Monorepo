package com.projecthub.mapper;

import com.projecthub.dto.WorklogCreateRequest;
import com.projecthub.dto.WorklogDto;
import com.projecthub.entity.Worklog;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorklogMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "taskNo", source = "task.taskNo")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.displayName")
    WorklogDto toDto(Worklog worklog);

    List<WorklogDto> toDtoList(List<Worklog> worklogs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Worklog toEntity(WorklogCreateRequest request);
}

package com.projecthub.dto;

import com.projecthub.entity.enums.RequirementStatus;
import com.projecthub.entity.enums.TaskPriority;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequirementDto(
        UUID id,
        String reqNo,
        UUID projectId,
        String projectName,
        String title,
        String description,
        TaskPriority priority,
        RequirementStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

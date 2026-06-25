package com.projecthub.dto;

import com.projecthub.entity.enums.BugSeverity;
import com.projecthub.entity.enums.BugStatus;
import com.projecthub.entity.enums.TaskPriority;

import java.time.LocalDateTime;
import java.util.UUID;

public record BugDto(
        UUID id,
        String bugNo,
        UUID projectId,
        String projectName,
        String title,
        String description,
        BugSeverity severity,
        TaskPriority priority,
        BugStatus status,
        UUID assigneeId,
        String assigneeName,
        UUID reporterId,
        String reporterName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

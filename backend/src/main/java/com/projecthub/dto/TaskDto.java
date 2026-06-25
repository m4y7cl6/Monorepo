package com.projecthub.dto;

import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.entity.enums.TaskType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(
        UUID id,
        String taskNo,
        UUID projectId,
        String projectName,
        UUID sprintId,
        String sprintName,
        String title,
        String description,
        TaskType taskType,
        TaskPriority priority,
        TaskStatus status,
        UUID assigneeId,
        String assigneeName,
        UUID reporterId,
        String reporterName,
        BigDecimal estimateHours,
        BigDecimal actualHours,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

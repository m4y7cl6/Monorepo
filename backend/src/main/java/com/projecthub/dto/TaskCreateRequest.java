package com.projecthub.dto;

import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.entity.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TaskCreateRequest(
        @NotBlank(message = "Task number is required")
        @Size(max = 50)
        String taskNo,

        @NotNull(message = "Project ID is required")
        UUID projectId,

        UUID sprintId,

        @NotBlank(message = "Task title is required")
        @Size(max = 500, message = "Task title must not exceed 500 characters")
        String title,

        String description,

        TaskType taskType,

        TaskPriority priority,

        TaskStatus status,

        UUID assigneeId,

        UUID reporterId,

        @Positive(message = "Estimate hours must be positive")
        BigDecimal estimateHours,

        LocalDate dueDate
) {}

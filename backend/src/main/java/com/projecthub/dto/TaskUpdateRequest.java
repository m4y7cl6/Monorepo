package com.projecthub.dto;

import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.entity.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TaskUpdateRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 500, message = "Task title must not exceed 500 characters")
        String title,

        String description,

        TaskType taskType,

        TaskPriority priority,

        TaskStatus status,

        UUID sprintId,

        UUID assigneeId,

        UUID reporterId,

        @Positive(message = "Estimate hours must be positive")
        BigDecimal estimateHours,

        @Positive(message = "Actual hours must be positive")
        BigDecimal actualHours,

        LocalDate dueDate
) {}

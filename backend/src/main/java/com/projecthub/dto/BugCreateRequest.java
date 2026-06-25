package com.projecthub.dto;

import com.projecthub.entity.enums.BugSeverity;
import com.projecthub.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BugCreateRequest(
        @NotBlank(message = "Bug number is required")
        @Size(max = 50)
        String bugNo,

        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotBlank(message = "Bug title is required")
        @Size(max = 500, message = "Bug title must not exceed 500 characters")
        String title,

        String description,

        BugSeverity severity,

        TaskPriority priority,

        UUID assigneeId,

        UUID reporterId
) {}

package com.projecthub.dto;

import com.projecthub.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequirementCreateRequest(
        @NotBlank(message = "Requirement number is required")
        @Size(max = 50)
        String reqNo,

        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotBlank(message = "Requirement title is required")
        @Size(max = 500, message = "Requirement title must not exceed 500 characters")
        String title,

        String description,

        TaskPriority priority
) {}

package com.projecthub.dto;

import com.projecthub.entity.enums.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record SprintCreateRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotBlank(message = "Sprint name is required")
        @Size(max = 255, message = "Sprint name must not exceed 255 characters")
        String name,

        LocalDate startDate,

        LocalDate endDate,

        String goal,

        SprintStatus status
) {}

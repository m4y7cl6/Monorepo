package com.projecthub.dto;

import com.projecthub.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        String name,

        String description,

        @Size(max = 255)
        String customer,

        LocalDate startDate,

        LocalDate endDate,

        ProjectStatus status
) {}

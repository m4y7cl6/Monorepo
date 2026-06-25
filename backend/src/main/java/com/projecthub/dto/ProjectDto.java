package com.projecthub.dto;

import com.projecthub.entity.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String code,
        String name,
        String description,
        String customer,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

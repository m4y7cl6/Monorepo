package com.projecthub.dto;

import com.projecthub.entity.enums.SprintStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SprintDto(
        UUID id,
        UUID projectId,
        String projectName,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String goal,
        SprintStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

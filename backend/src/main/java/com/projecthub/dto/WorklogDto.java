package com.projecthub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorklogDto(
        UUID id,
        UUID taskId,
        String taskTitle,
        String taskNo,
        UUID userId,
        String userName,
        LocalDate workDate,
        BigDecimal hours,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

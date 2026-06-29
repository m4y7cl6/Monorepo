package com.projecthub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorklogCreateRequest(
        @NotNull(message = "Task ID is required")
        UUID taskId,

        UUID userId,

        @NotNull(message = "Work date is required")
        LocalDate workDate,

        @NotNull(message = "Hours is required")
        @Positive(message = "Hours must be positive")
        BigDecimal hours,

        String description
) {}

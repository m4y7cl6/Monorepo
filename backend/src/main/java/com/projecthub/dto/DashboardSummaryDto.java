package com.projecthub.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        long totalProjects,
        long activeProjects,
        long totalTasks,
        long backlogTasks,
        long openTasks,
        long inProgressTasks,
        long reviewTasks,
        long testingTasks,
        long doneTasks,
        long totalBugs,
        long openBugs,
        long criticalBugs,
        long totalRequirements,
        long approvedRequirements,
        long totalWorklogs,
        BigDecimal totalHoursLogged
) {}

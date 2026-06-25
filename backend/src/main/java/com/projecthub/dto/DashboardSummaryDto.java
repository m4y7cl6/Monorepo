package com.projecthub.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryDto(
        ProjectSummary projectSummary,
        TaskSummary taskSummary,
        BugSummary bugSummary,
        WorklogSummary worklogSummary
) {

    public record ProjectSummary(
            long total,
            long planning,
            long development,
            long testing,
            long uat,
            long production,
            long closed
    ) {}

    public record TaskSummary(
            long total,
            long backlog,
            long todo,
            long inProgress,
            long review,
            long testing,
            long done,
            Map<String, Long> byProject
    ) {}

    public record BugSummary(
            long total,
            long open,
            long inProgress,
            long resolved,
            long closed,
            long critical,
            long high
    ) {}

    public record WorklogSummary(
            BigDecimal totalHoursThisWeek,
            BigDecimal totalHoursThisMonth,
            long activeContributors
    ) {}
}

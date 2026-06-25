package com.projecthub.service;

import com.projecthub.dto.DashboardSummaryDto;
import com.projecthub.entity.enums.BugSeverity;
import com.projecthub.entity.enums.BugStatus;

import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.repository.BugRepository;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.TaskRepository;
import com.projecthub.repository.WorklogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final BugRepository bugRepository;
    private final WorklogRepository worklogRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            BugRepository bugRepository,
                            WorklogRepository worklogRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.bugRepository = bugRepository;
        this.worklogRepository = worklogRepository;
    }

    public DashboardSummaryDto getSummary() {
        log.debug("Computing dashboard summary");

        return new DashboardSummaryDto(
                buildProjectSummary(),
                buildTaskSummary(),
                buildBugSummary(),
                buildWorklogSummary()
        );
    }

    private DashboardSummaryDto.ProjectSummary buildProjectSummary() {
        long total = projectRepository.count();
        long planning = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.PLANNING);
        long development = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.DEVELOPMENT);
        long testing = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.TESTING);
        long uat = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.UAT);
        long production = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.PRODUCTION);
        long closed = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.CLOSED);

        return new DashboardSummaryDto.ProjectSummary(
                total, planning, development, testing, uat, production, closed);
    }

    private DashboardSummaryDto.TaskSummary buildTaskSummary() {
        long total = taskRepository.count();
        long backlog = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.BACKLOG);
        long todo = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.TODO);
        long inProgress = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.IN_PROGRESS);
        long review = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.REVIEW);
        long testing = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.TESTING);
        long done = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.DONE);

        Map<String, Long> byProject = new HashMap<>();

        return new DashboardSummaryDto.TaskSummary(
                total, backlog, todo, inProgress, review, testing, done, byProject);
    }

    private DashboardSummaryDto.BugSummary buildBugSummary() {
        long total = bugRepository.count();
        long open = bugRepository.countByStatusAndDeletedAtIsNull(BugStatus.OPEN);
        long inProgress = bugRepository.countByStatusAndDeletedAtIsNull(BugStatus.IN_PROGRESS);
        long resolved = bugRepository.countByStatusAndDeletedAtIsNull(BugStatus.RESOLVED);
        long closed = bugRepository.countByStatusAndDeletedAtIsNull(BugStatus.CLOSED);
        long critical = bugRepository.countBySeverityAndDeletedAtIsNull(BugSeverity.CRITICAL);
        long high = bugRepository.countBySeverityAndDeletedAtIsNull(BugSeverity.HIGH);

        return new DashboardSummaryDto.BugSummary(
                total, open, inProgress, resolved, closed, critical, high);
    }

    private DashboardSummaryDto.WorklogSummary buildWorklogSummary() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal hoursThisWeek = worklogRepository.sumHoursByDateRange(weekStart, weekEnd);
        BigDecimal hoursThisMonth = worklogRepository.sumHoursByDateRange(monthStart, monthEnd);

        if (hoursThisWeek == null) {
            hoursThisWeek = BigDecimal.ZERO;
        }
        if (hoursThisMonth == null) {
            hoursThisMonth = BigDecimal.ZERO;
        }

        long activeContributors = worklogRepository.findByWorkDateBetween(weekStart, weekEnd)
                .stream()
                .map(w -> w.getUser().getId())
                .distinct()
                .count();

        return new DashboardSummaryDto.WorklogSummary(
                hoursThisWeek, hoursThisMonth, activeContributors);
    }
}

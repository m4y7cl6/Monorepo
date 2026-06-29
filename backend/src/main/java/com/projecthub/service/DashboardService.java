package com.projecthub.service;

import com.projecthub.dto.DashboardSummaryDto;
import com.projecthub.entity.enums.BugSeverity;
import com.projecthub.entity.enums.BugStatus;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.entity.enums.RequirementStatus;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.repository.BugRepository;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.RequirementRepository;
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

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final BugRepository bugRepository;
    private final RequirementRepository requirementRepository;
    private final WorklogRepository worklogRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            BugRepository bugRepository,
                            RequirementRepository requirementRepository,
                            WorklogRepository worklogRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.bugRepository = bugRepository;
        this.requirementRepository = requirementRepository;
        this.worklogRepository = worklogRepository;
    }

    public DashboardSummaryDto getSummary() {
        log.debug("Computing dashboard summary");

        // Projects
        long totalProjects = projectRepository.countByDeletedAtIsNull();
        long closedProjects = projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.CLOSED);
        long activeProjects = totalProjects - closedProjects;

        // Tasks
        long totalTasks = taskRepository.countByDeletedAtIsNull();
        long backlogTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.BACKLOG);
        long openTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.IN_PROGRESS);
        long reviewTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.REVIEW);
        long testingTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.TESTING);
        long doneTasks = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.DONE);

        // Bugs
        long totalBugs = bugRepository.countByDeletedAtIsNull();
        long openBugs = bugRepository.countByStatusAndDeletedAtIsNull(BugStatus.NEW);
        long criticalBugs = bugRepository.countBySeverityAndDeletedAtIsNull(BugSeverity.CRITICAL);

        // Requirements
        long totalRequirements = requirementRepository.countByDeletedAtIsNull();
        long approvedRequirements = requirementRepository.countByStatusAndDeletedAtIsNull(RequirementStatus.APPROVED);

        // Worklogs — total count and hours this week
        long totalWorklogs = worklogRepository.count();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        BigDecimal hoursThisWeek = worklogRepository.sumHoursByDateRange(weekStart, weekEnd);
        if (hoursThisWeek == null) {
            hoursThisWeek = BigDecimal.ZERO;
        }

        return new DashboardSummaryDto(
                totalProjects, activeProjects,
                totalTasks, backlogTasks, openTasks, inProgressTasks, reviewTasks, testingTasks, doneTasks,
                totalBugs, openBugs, criticalBugs,
                totalRequirements, approvedRequirements,
                totalWorklogs, hoursThisWeek);
    }
}

package com.projecthub;

import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectDto;
import com.projecthub.dto.SprintDto;
import com.projecthub.dto.TaskCreateRequest;
import com.projecthub.dto.TaskDto;
import com.projecthub.dto.WorklogDto;
import com.projecthub.entity.Project;
import com.projecthub.entity.Sprint;
import com.projecthub.entity.Task;
import com.projecthub.entity.User;
import com.projecthub.entity.Worklog;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.entity.enums.SprintStatus;
import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.entity.enums.TaskType;
import com.projecthub.entity.enums.UserRole;
import com.projecthub.entity.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Central factory for test data construction.
 * Provides deterministic, fully-populated entities and DTOs for use across all test classes.
 */
public final class TestDataFactory {

    public static final UUID PROJECT_ID   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID TASK_ID      = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID SPRINT_ID    = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID USER_ID      = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID WORKLOG_ID   = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private TestDataFactory() {}

    // ------------------------------------------------------------------ Project

    public static Project createProject() {
        return Project.builder()
                .id(PROJECT_ID)
                .code("TEST-PRJ")
                .name("Test Project")
                .description("A project used in tests")
                .customer("Test Customer")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    public static ProjectDto createProjectDto() {
        return new ProjectDto(
                PROJECT_ID,
                "TEST-PRJ",
                "Test Project",
                "A project used in tests",
                "Test Customer",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                ProjectStatus.PLANNING,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    public static ProjectCreateRequest createProjectCreateRequest() {
        return new ProjectCreateRequest(
                "TEST-PRJ",
                "Test Project",
                "A project used in tests",
                "Test Customer",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                ProjectStatus.PLANNING
        );
    }

    // ------------------------------------------------------------------ Task

    public static Task createTask(Project project) {
        return Task.builder()
                .id(TASK_ID)
                .taskNo("TASK-001")
                .project(project)
                .title("Test Task")
                .description("A task used in tests")
                .taskType(TaskType.TASK)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.BACKLOG)
                .estimateHours(new BigDecimal("4.00"))
                .dueDate(LocalDate.of(2026, 3, 31))
                .createdAt(LocalDateTime.of(2026, 1, 2, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 2, 0, 0))
                .build();
    }

    public static TaskDto createTaskDto() {
        return new TaskDto(
                TASK_ID,
                "TASK-001",
                PROJECT_ID,
                "Test Project",
                null,
                null,
                "Test Task",
                "A task used in tests",
                TaskType.TASK,
                TaskPriority.MEDIUM,
                TaskStatus.BACKLOG,
                null,
                null,
                null,
                null,
                new BigDecimal("4.00"),
                null,
                LocalDate.of(2026, 3, 31),
                LocalDateTime.of(2026, 1, 2, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0)
        );
    }

    public static TaskCreateRequest createTaskCreateRequest(UUID projectId) {
        return new TaskCreateRequest(
                "TASK-001",
                projectId,
                null,
                "Test Task",
                "A task used in tests",
                TaskType.TASK,
                TaskPriority.MEDIUM,
                TaskStatus.BACKLOG,
                null,
                null,
                new BigDecimal("4.00"),
                LocalDate.of(2026, 3, 31)
        );
    }

    // ------------------------------------------------------------------ Sprint

    public static Sprint createSprint(Project project) {
        return Sprint.builder()
                .id(SPRINT_ID)
                .project(project)
                .sprintName("Sprint 1")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 14))
                .goal("Deliver MVP features")
                .status(SprintStatus.PLANNED)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    public static SprintDto createSprintDto() {
        return new SprintDto(
                SPRINT_ID,
                PROJECT_ID,
                "Test Project",
                "Sprint 1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 14),
                "Deliver MVP features",
                SprintStatus.PLANNED,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    // ------------------------------------------------------------------ User

    public static User createUser() {
        return User.builder()
                .id(USER_ID)
                .username("testuser")
                .email("testuser@projecthub.test")
                .displayName("Test User")
                .role(UserRole.DEVELOPER)
                .status(UserStatus.ACTIVE)
                .keycloakId("kc-test-user-id")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    // ------------------------------------------------------------------ Worklog

    public static Worklog createWorklog(Task task, User user) {
        return Worklog.builder()
                .id(WORKLOG_ID)
                .task(task)
                .user(user)
                .workDate(LocalDate.of(2026, 1, 5))
                .hours(new BigDecimal("3.50"))
                .description("Implemented feature X")
                .createdAt(LocalDateTime.of(2026, 1, 5, 9, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 5, 9, 0))
                .build();
    }

    public static WorklogDto createWorklogDto() {
        return new WorklogDto(
                WORKLOG_ID,
                TASK_ID,
                "Test Task",
                "TASK-001",
                USER_ID,
                "testuser",
                LocalDate.of(2026, 1, 5),
                new BigDecimal("3.50"),
                "Implemented feature X",
                LocalDateTime.of(2026, 1, 5, 9, 0),
                LocalDateTime.of(2026, 1, 5, 9, 0)
        );
    }
}

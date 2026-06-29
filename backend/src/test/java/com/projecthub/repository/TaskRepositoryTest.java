package com.projecthub.repository;

import com.projecthub.entity.Project;
import com.projecthub.entity.Sprint;
import com.projecthub.entity.Task;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.entity.enums.SprintStatus;
import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.entity.enums.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository @DataJpaTest")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .code("REPO-TST")
                .name("Repo Test Project")
                .status(ProjectStatus.PLANNING)
                .build();
        project = entityManager.persistAndFlush(project);

        sprint = Sprint.builder()
                .project(project)
                .sprintName("Sprint 1")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 14))
                .status(SprintStatus.PLANNED)
                .build();
        sprint = entityManager.persistAndFlush(sprint);
    }

    private Task buildTask(String taskNo, TaskStatus status, Sprint sprintRef, boolean deleted) {
        Task.TaskBuilder builder = Task.builder()
                .taskNo(taskNo)
                .project(project)
                .title("Task " + taskNo)
                .taskType(TaskType.TASK)
                .priority(TaskPriority.MEDIUM)
                .status(status)
                .estimateHours(new BigDecimal("4.00"));
        if (sprintRef != null) {
            builder.sprint(sprintRef);
        }
        if (deleted) {
            builder.deletedAt(LocalDateTime.now());
        }
        return builder.build();
    }

    // ------------------------------------------------------------------ findByProjectId

    @Nested
    @DisplayName("findByProjectIdAndDeletedAtIsNull")
    class FindByProjectId {

        @Test
        @DisplayName("findByProjectId_returnsOnlyNonDeletedTasksForProject")
        void findByProjectId_returnsOnlyNonDeletedTasksForProject() {
            Task active  = entityManager.persistAndFlush(buildTask("T-001", TaskStatus.TODO, null, false));
            Task deleted = entityManager.persistAndFlush(buildTask("T-002", TaskStatus.DONE, null, true));

            List<Task> result = taskRepository.findByProjectIdAndDeletedAtIsNull(project.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTaskNo()).isEqualTo("T-001");
        }

        @Test
        @DisplayName("findByProjectId_paginated_returnsCorrectPage")
        void findByProjectId_paginated_returnsCorrectPage() {
            entityManager.persistAndFlush(buildTask("T-P01", TaskStatus.BACKLOG, null, false));
            entityManager.persistAndFlush(buildTask("T-P02", TaskStatus.BACKLOG, null, false));
            entityManager.persistAndFlush(buildTask("T-P03", TaskStatus.BACKLOG, null, false));

            Page<Task> page = taskRepository.findByProjectIdAndDeletedAtIsNull(
                    project.getId(), PageRequest.of(0, 2));

            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("findByProjectId_whenNoTasks_returnsEmpty")
        void findByProjectId_whenNoTasks_returnsEmpty() {
            List<Task> result = taskRepository.findByProjectIdAndDeletedAtIsNull(project.getId());
            assertThat(result).isEmpty();
        }
    }

    // ------------------------------------------------------------------ findBySprintId

    @Nested
    @DisplayName("findBySprintIdAndDeletedAtIsNull")
    class FindBySprintId {

        @Test
        @DisplayName("findBySprintId_returnsTasksAssignedToSprint")
        void findBySprintId_returnsTasksAssignedToSprint() {
            entityManager.persistAndFlush(buildTask("S-001", TaskStatus.TODO, sprint, false));
            entityManager.persistAndFlush(buildTask("S-002", TaskStatus.BACKLOG, null, false));

            List<Task> result = taskRepository.findBySprintIdAndDeletedAtIsNull(sprint.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTaskNo()).isEqualTo("S-001");
        }

        @Test
        @DisplayName("findBySprintId_excludesDeletedTasks")
        void findBySprintId_excludesDeletedTasks() {
            entityManager.persistAndFlush(buildTask("S-D01", TaskStatus.TODO, sprint, true));

            List<Task> result = taskRepository.findBySprintIdAndDeletedAtIsNull(sprint.getId());

            assertThat(result).isEmpty();
        }
    }

    // ------------------------------------------------------------------ countByStatus

    @Nested
    @DisplayName("countByStatusAndDeletedAtIsNull")
    class CountByStatus {

        @Test
        @DisplayName("countByStatusAndDeletedAtIsNull_countsOnlyNonDeletedMatchingStatus")
        void countByStatusAndDeletedAtIsNull_countsOnlyNonDeletedMatchingStatus() {
            entityManager.persistAndFlush(buildTask("C-001", TaskStatus.DONE, null, false));
            entityManager.persistAndFlush(buildTask("C-002", TaskStatus.DONE, null, false));
            entityManager.persistAndFlush(buildTask("C-003", TaskStatus.DONE, null, true));  // deleted
            entityManager.persistAndFlush(buildTask("C-004", TaskStatus.IN_PROGRESS, null, false));

            long count = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.DONE);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("countByStatusAndDeletedAtIsNull_returnsZeroWhenNoneMatch")
        void countByStatusAndDeletedAtIsNull_returnsZeroWhenNoneMatch() {
            long count = taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.REVIEW);
            assertThat(count).isZero();
        }
    }

    // ------------------------------------------------------------------ existsByTaskNo

    @Nested
    @DisplayName("existsByTaskNo")
    class ExistsByTaskNo {

        @Test
        @DisplayName("existsByTaskNo_whenTaskExists_returnsTrue")
        void existsByTaskNo_whenTaskExists_returnsTrue() {
            entityManager.persistAndFlush(buildTask("UNIQUE-01", TaskStatus.BACKLOG, null, false));
            assertThat(taskRepository.existsByTaskNo("UNIQUE-01")).isTrue();
        }

        @Test
        @DisplayName("existsByTaskNo_whenTaskDoesNotExist_returnsFalse")
        void existsByTaskNo_whenTaskDoesNotExist_returnsFalse() {
            assertThat(taskRepository.existsByTaskNo("MISSING-99")).isFalse();
        }
    }

    // ------------------------------------------------------------------ findByIdAndDeletedAtIsNull

    @Nested
    @DisplayName("findByIdAndDeletedAtIsNull")
    class FindByIdNotDeleted {

        @Test
        @DisplayName("findByIdAndDeletedAtIsNull_whenActive_returnsTask")
        void findByIdAndDeletedAtIsNull_whenActive_returnsTask() {
            Task saved = entityManager.persistAndFlush(buildTask("ID-01", TaskStatus.TODO, null, false));

            Optional<Task> found = taskRepository.findByIdAndDeletedAtIsNull(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTaskNo()).isEqualTo("ID-01");
        }

        @Test
        @DisplayName("findByIdAndDeletedAtIsNull_whenDeleted_returnsEmpty")
        void findByIdAndDeletedAtIsNull_whenDeleted_returnsEmpty() {
            Task saved = entityManager.persistAndFlush(buildTask("ID-02", TaskStatus.DONE, null, true));

            Optional<Task> found = taskRepository.findByIdAndDeletedAtIsNull(saved.getId());

            assertThat(found).isEmpty();
        }
    }

    // ------------------------------------------------------------------ findBacklogTasksByProjectId

    @Nested
    @DisplayName("findBacklogTasksByProjectId")
    class FindBacklogTasks {

        @Test
        @DisplayName("findBacklogTasksByProjectId_returnsOnlyTasksWithNoSprint")
        void findBacklogTasksByProjectId_returnsOnlyTasksWithNoSprint() {
            entityManager.persistAndFlush(buildTask("BL-001", TaskStatus.BACKLOG, null, false));
            entityManager.persistAndFlush(buildTask("BL-002", TaskStatus.BACKLOG, sprint, false));

            List<Task> result = taskRepository.findBacklogTasksByProjectId(project.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTaskNo()).isEqualTo("BL-001");
        }
    }
}

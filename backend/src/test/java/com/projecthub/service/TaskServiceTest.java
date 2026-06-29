package com.projecthub.service;

import com.projecthub.TestDataFactory;
import com.projecthub.dto.PageResponse;
import com.projecthub.dto.TaskCreateRequest;
import com.projecthub.dto.TaskDto;
import com.projecthub.dto.TaskUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.Task;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.TaskMapper;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.SprintRepository;
import com.projecthub.repository.TaskRepository;
import com.projecthub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService unit tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private SprintRepository sprintRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks private TaskService taskService;

    private Project project;
    private Task task;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        project = TestDataFactory.createProject();
        task    = TestDataFactory.createTask(project);
        taskDto = TestDataFactory.createTaskDto();
    }

    // ------------------------------------------------------------------ findAll

    @Nested
    @DisplayName("findAll(Pageable)")
    class FindAll {

        @Test
        @DisplayName("findAll_withExistingTasks_returnsPageResponse")
        void findAll_withExistingTasks_returnsPageResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

            when(taskRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            PageResponse<TaskDto> result = taskService.findAll(pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).taskNo()).isEqualTo("TASK-001");
        }

        @Test
        @DisplayName("findAll_withEmptyRepository_returnsEmptyPageResponse")
        void findAll_withEmptyRepository_returnsEmptyPageResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Task> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(taskRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(emptyPage);

            PageResponse<TaskDto> result = taskService.findAll(pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    // ------------------------------------------------------------------ findById

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("findById_whenTaskExists_returnsTaskDto")
        void findById_whenTaskExists_returnsTaskDto() {
            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            TaskDto result = taskService.findById(TestDataFactory.TASK_ID);

            assertThat(result.id()).isEqualTo(TestDataFactory.TASK_ID);
            assertThat(result.taskNo()).isEqualTo("TASK-001");
        }

        @Test
        @DisplayName("findById_whenNotFound_throwsResourceNotFoundException")
        void findById_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(taskRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.findById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");
        }
    }

    // ------------------------------------------------------------------ findByProjectId

    @Nested
    @DisplayName("findByProjectId")
    class FindByProject {

        @Test
        @DisplayName("findByProjectId_whenProjectHasTasks_returnsTaskDtoList")
        void findByProjectId_whenProjectHasTasks_returnsTaskDtoList() {
            when(taskRepository.findByProjectIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(List.of(task));
            when(taskMapper.toDtoList(List.of(task))).thenReturn(List.of(taskDto));

            List<TaskDto> result = taskService.findByProjectId(TestDataFactory.PROJECT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).projectId()).isEqualTo(TestDataFactory.PROJECT_ID);
        }

        @Test
        @DisplayName("findByProjectId_paginated_returnsPageResponse")
        void findByProjectId_paginated_returnsPageResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);
            when(taskRepository.findByProjectIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID, pageable))
                    .thenReturn(page);
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            PageResponse<TaskDto> result = taskService.findByProjectId(TestDataFactory.PROJECT_ID, pageable);

            assertThat(result.content()).hasSize(1);
        }
    }

    // ------------------------------------------------------------------ create

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create_withValidRequest_savesAndReturnsTaskDto")
        void create_withValidRequest_savesAndReturnsTaskDto() {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);

            when(taskRepository.existsByTaskNo("TASK-001")).thenReturn(false);
            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(taskMapper.toEntity(request)).thenReturn(task);
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            TaskDto result = taskService.create(request);

            assertThat(result.taskNo()).isEqualTo("TASK-001");
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("create_withDuplicateTaskNo_throwsBusinessException")
        void create_withDuplicateTaskNo_throwsBusinessException() {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);
            when(taskRepository.existsByTaskNo("TASK-001")).thenReturn(true);

            assertThatThrownBy(() -> taskService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("TASK-001");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("create_whenProjectNotFound_throwsResourceNotFoundException")
        void create_whenProjectNotFound_throwsResourceNotFoundException() {
            UUID missingProjectId = UUID.randomUUID();
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(missingProjectId);

            when(taskRepository.existsByTaskNo("TASK-001")).thenReturn(false);
            when(projectRepository.findByIdAndDeletedAtIsNull(missingProjectId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project");
        }

        @Test
        @DisplayName("create_withNullStatus_defaultsToBacklog")
        void create_withNullStatus_defaultsToBacklog() {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);
            Task taskWithoutStatus = TestDataFactory.createTask(project);
            taskWithoutStatus.setStatus(null);

            when(taskRepository.existsByTaskNo("TASK-001")).thenReturn(false);
            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(taskMapper.toEntity(request)).thenReturn(taskWithoutStatus);
            when(taskRepository.save(taskWithoutStatus)).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            taskService.create(request);

            assertThat(taskWithoutStatus.getStatus()).isEqualTo(TaskStatus.BACKLOG);
        }
    }

    // ------------------------------------------------------------------ updateStatus

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("updateStatus_whenTaskExists_transitionsStatusAndReturnsDto")
        void updateStatus_whenTaskExists_transitionsStatusAndReturnsDto() {
            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskDto);

            TaskDto result = taskService.updateStatus(TestDataFactory.TASK_ID, TaskStatus.IN_PROGRESS);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("updateStatus_whenNotFound_throwsResourceNotFoundException")
        void updateStatus_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(taskRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateStatus(unknownId, TaskStatus.DONE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");
        }
    }

    // ------------------------------------------------------------------ softDelete

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("softDelete_whenTaskExists_setsDeletedAt")
        void softDelete_whenTaskExists_setsDeletedAt() {
            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            taskService.softDelete(TestDataFactory.TASK_ID);

            assertThat(task.getDeletedAt()).isNotNull();
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("softDelete_whenNotFound_throwsResourceNotFoundException")
        void softDelete_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(taskRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.softDelete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskRepository, never()).save(any());
        }
    }
}

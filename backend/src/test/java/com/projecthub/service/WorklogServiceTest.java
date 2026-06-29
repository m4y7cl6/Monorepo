package com.projecthub.service;

import com.projecthub.TestDataFactory;
import com.projecthub.dto.WorklogCreateRequest;
import com.projecthub.dto.WorklogDto;
import com.projecthub.entity.Task;
import com.projecthub.entity.User;
import com.projecthub.entity.Worklog;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.WorklogMapper;
import com.projecthub.repository.TaskRepository;
import com.projecthub.repository.UserRepository;
import com.projecthub.repository.WorklogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorklogService unit tests")
class WorklogServiceTest {

    @Mock private WorklogRepository worklogRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorklogMapper worklogMapper;

    @InjectMocks private WorklogService worklogService;

    private Task task;
    private User user;
    private Worklog worklog;
    private WorklogDto worklogDto;

    @BeforeEach
    void setUp() {
        user     = TestDataFactory.createUser();
        task     = TestDataFactory.createTask(TestDataFactory.createProject());
        worklog  = TestDataFactory.createWorklog(task, user);
        worklogDto = TestDataFactory.createWorklogDto();
    }

    // ------------------------------------------------------------------ create

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create_withValidRequest_savesAndReturnsWorklogDto")
        void create_withValidRequest_savesAndReturnsWorklogDto() {
            WorklogCreateRequest request = new WorklogCreateRequest(
                    TestDataFactory.TASK_ID, TestDataFactory.USER_ID,
                    LocalDate.of(2026, 1, 5), new BigDecimal("3.50"),
                    "Implemented feature X");

            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(userRepository.findById(TestDataFactory.USER_ID))
                    .thenReturn(Optional.of(user));
            when(worklogMapper.toEntity(request)).thenReturn(worklog);
            when(worklogRepository.save(worklog)).thenReturn(worklog);
            when(worklogMapper.toDto(worklog)).thenReturn(worklogDto);

            WorklogDto result = worklogService.create(request);

            assertThat(result.hours()).isEqualByComparingTo(new BigDecimal("3.50"));
            verify(worklogRepository).save(worklog);
        }

        @Test
        @DisplayName("create_whenTaskNotFound_throwsResourceNotFoundException")
        void create_whenTaskNotFound_throwsResourceNotFoundException() {
            UUID missingTaskId = UUID.randomUUID();
            WorklogCreateRequest request = new WorklogCreateRequest(
                    missingTaskId, TestDataFactory.USER_ID,
                    LocalDate.of(2026, 1, 5), new BigDecimal("2.00"), null);

            when(taskRepository.findByIdAndDeletedAtIsNull(missingTaskId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> worklogService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(worklogRepository, never()).save(any());
        }

        @Test
        @DisplayName("create_whenUserNotFound_throwsResourceNotFoundException")
        void create_whenUserNotFound_throwsResourceNotFoundException() {
            UUID missingUserId = UUID.randomUUID();
            WorklogCreateRequest request = new WorklogCreateRequest(
                    TestDataFactory.TASK_ID, missingUserId,
                    LocalDate.of(2026, 1, 5), new BigDecimal("2.00"), null);

            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(userRepository.findById(missingUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> worklogService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }

        @Test
        @DisplayName("create_whenUserIsDeleted_throwsResourceNotFoundException")
        void create_whenUserIsDeleted_throwsResourceNotFoundException() {
            user.setDeletedAt(java.time.LocalDateTime.now());
            WorklogCreateRequest request = new WorklogCreateRequest(
                    TestDataFactory.TASK_ID, TestDataFactory.USER_ID,
                    LocalDate.of(2026, 1, 5), new BigDecimal("1.00"), null);

            when(taskRepository.findByIdAndDeletedAtIsNull(TestDataFactory.TASK_ID))
                    .thenReturn(Optional.of(task));
            when(userRepository.findById(TestDataFactory.USER_ID))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() -> worklogService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    // ------------------------------------------------------------------ findByTaskId

    @Nested
    @DisplayName("findByTaskId")
    class FindByTask {

        @Test
        @DisplayName("findByTaskId_withExistingWorklogs_returnsDtoList")
        void findByTaskId_withExistingWorklogs_returnsDtoList() {
            when(worklogRepository.findByTaskId(TestDataFactory.TASK_ID))
                    .thenReturn(List.of(worklog));
            when(worklogMapper.toDtoList(List.of(worklog)))
                    .thenReturn(List.of(worklogDto));

            List<WorklogDto> result = worklogService.findByTaskId(TestDataFactory.TASK_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).taskId()).isEqualTo(TestDataFactory.TASK_ID);
        }

        @Test
        @DisplayName("findByTaskId_withNoWorklogs_returnsEmptyList")
        void findByTaskId_withNoWorklogs_returnsEmptyList() {
            when(worklogRepository.findByTaskId(TestDataFactory.TASK_ID))
                    .thenReturn(List.of());
            when(worklogMapper.toDtoList(List.of())).thenReturn(List.of());

            List<WorklogDto> result = worklogService.findByTaskId(TestDataFactory.TASK_ID);

            assertThat(result).isEmpty();
        }
    }

    // ------------------------------------------------------------------ findByDateRange

    @Nested
    @DisplayName("findByDateRange")
    class FindByDateRange {

        @Test
        @DisplayName("findByDateRange_withMatchingWorklogs_returnsDtoList")
        void findByDateRange_withMatchingWorklogs_returnsDtoList() {
            LocalDate from = LocalDate.of(2026, 1, 1);
            LocalDate to   = LocalDate.of(2026, 1, 31);

            when(worklogRepository.findByWorkDateBetween(from, to))
                    .thenReturn(List.of(worklog));
            when(worklogMapper.toDtoList(List.of(worklog)))
                    .thenReturn(List.of(worklogDto));

            List<WorklogDto> result = worklogService.findByDateRange(from, to);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("findByDateRange_withNoMatchingWorklogs_returnsEmptyList")
        void findByDateRange_withNoMatchingWorklogs_returnsEmptyList() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to   = LocalDate.of(2025, 1, 31);

            when(worklogRepository.findByWorkDateBetween(from, to))
                    .thenReturn(List.of());
            when(worklogMapper.toDtoList(List.of())).thenReturn(List.of());

            List<WorklogDto> result = worklogService.findByDateRange(from, to);

            assertThat(result).isEmpty();
        }
    }

    // ------------------------------------------------------------------ update

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update_whenWorklogExists_updatesAndReturnsDto")
        void update_whenWorklogExists_updatesAndReturnsDto() {
            WorklogCreateRequest request = new WorklogCreateRequest(
                    TestDataFactory.TASK_ID, TestDataFactory.USER_ID,
                    LocalDate.of(2026, 1, 6), new BigDecimal("5.00"),
                    "Updated description");

            when(worklogRepository.findById(TestDataFactory.WORKLOG_ID))
                    .thenReturn(Optional.of(worklog));
            when(worklogRepository.save(worklog)).thenReturn(worklog);
            when(worklogMapper.toDto(worklog)).thenReturn(worklogDto);

            WorklogDto result = worklogService.update(TestDataFactory.WORKLOG_ID, request);

            assertThat(result).isNotNull();
            assertThat(worklog.getHours()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(worklog.getWorkDate()).isEqualTo(LocalDate.of(2026, 1, 6));
            verify(worklogRepository).save(worklog);
        }

        @Test
        @DisplayName("update_whenNotFound_throwsResourceNotFoundException")
        void update_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            WorklogCreateRequest request = new WorklogCreateRequest(
                    TestDataFactory.TASK_ID, TestDataFactory.USER_ID,
                    LocalDate.of(2026, 1, 6), new BigDecimal("2.00"), null);

            when(worklogRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> worklogService.update(unknownId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Worklog");

            verify(worklogRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------ delete

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("delete_whenWorklogExists_deletesSuccessfully")
        void delete_whenWorklogExists_deletesSuccessfully() {
            when(worklogRepository.findById(TestDataFactory.WORKLOG_ID))
                    .thenReturn(Optional.of(worklog));

            worklogService.delete(TestDataFactory.WORKLOG_ID);

            verify(worklogRepository).delete(worklog);
        }

        @Test
        @DisplayName("delete_whenNotFound_throwsResourceNotFoundException")
        void delete_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(worklogRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> worklogService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Worklog");

            verify(worklogRepository, never()).delete(any());
        }
    }
}

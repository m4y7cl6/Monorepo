package com.projecthub.service;

import com.projecthub.TestDataFactory;
import com.projecthub.dto.SprintCreateRequest;
import com.projecthub.dto.SprintDto;
import com.projecthub.entity.Project;
import com.projecthub.entity.Sprint;
import com.projecthub.entity.enums.SprintStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.SprintMapper;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.SprintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SprintService unit tests")
class SprintServiceTest {

    @Mock private SprintRepository sprintRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private SprintMapper sprintMapper;

    @InjectMocks private SprintService sprintService;

    private Project project;
    private Sprint sprint;
    private SprintDto sprintDto;

    @BeforeEach
    void setUp() {
        project   = TestDataFactory.createProject();
        sprint    = TestDataFactory.createSprint(project);
        sprintDto = TestDataFactory.createSprintDto();
    }

    // ------------------------------------------------------------------ create

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create_withValidRequest_savesAndReturnsSprintDto")
        void create_withValidRequest_savesAndReturnsSprintDto() {
            SprintCreateRequest request = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID, "Sprint 1",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 14),
                    "MVP features", null);

            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(sprintMapper.toEntity(request)).thenReturn(sprint);
            when(sprintRepository.save(sprint)).thenReturn(sprint);
            when(sprintMapper.toDto(sprint)).thenReturn(sprintDto);

            SprintDto result = sprintService.create(request);

            assertThat(result.sprintName()).isEqualTo("Sprint 1");
            verify(sprintRepository).save(sprint);
        }

        @Test
        @DisplayName("create_whenProjectNotFound_throwsResourceNotFoundException")
        void create_whenProjectNotFound_throwsResourceNotFoundException() {
            UUID missingId = UUID.randomUUID();
            SprintCreateRequest request = new SprintCreateRequest(
                    missingId, "Sprint X",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 14),
                    null, null);

            when(projectRepository.findByIdAndDeletedAtIsNull(missingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sprintService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project");

            verify(sprintRepository, never()).save(any());
        }

        @Test
        @DisplayName("create_withNullStatus_defaultsToPlanned")
        void create_withNullStatus_defaultsToPlanned() {
            SprintCreateRequest request = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID, "Sprint 1",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 14),
                    "goal", null);

            Sprint sprintNoStatus = Sprint.builder()
                    .id(TestDataFactory.SPRINT_ID)
                    .project(project)
                    .sprintName("Sprint 1")
                    .status(null)
                    .build();

            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(sprintMapper.toEntity(request)).thenReturn(sprintNoStatus);
            when(sprintRepository.save(sprintNoStatus)).thenReturn(sprint);
            when(sprintMapper.toDto(sprint)).thenReturn(sprintDto);

            sprintService.create(request);

            assertThat(sprintNoStatus.getStatus()).isEqualTo(SprintStatus.PLANNED);
        }
    }

    // ------------------------------------------------------------------ activate

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activate_whenPlannedAndNoActiveSprint_setsStatusActive")
        void activate_whenPlannedAndNoActiveSprint_setsStatusActive() {
            sprint.setStatus(SprintStatus.PLANNED);

            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));
            when(sprintRepository.findByProjectIdAndStatus(TestDataFactory.PROJECT_ID, SprintStatus.ACTIVE))
                    .thenReturn(List.of());
            when(sprintRepository.save(sprint)).thenReturn(sprint);
            when(sprintMapper.toDto(sprint)).thenReturn(sprintDto);

            SprintDto result = sprintService.activate(TestDataFactory.SPRINT_ID);

            assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("activate_whenAlreadyActiveSprintExists_throwsBusinessException")
        void activate_whenAlreadyActiveSprintExists_throwsBusinessException() {
            sprint.setStatus(SprintStatus.PLANNED);
            Sprint existingActive = Sprint.builder()
                    .id(UUID.randomUUID())
                    .project(project)
                    .sprintName("Running Sprint")
                    .status(SprintStatus.ACTIVE)
                    .build();

            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));
            when(sprintRepository.findByProjectIdAndStatus(TestDataFactory.PROJECT_ID, SprintStatus.ACTIVE))
                    .thenReturn(List.of(existingActive));

            assertThatThrownBy(() -> sprintService.activate(TestDataFactory.SPRINT_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("active sprint");

            verify(sprintRepository, never()).save(any());
        }

        @Test
        @DisplayName("activate_whenSprintNotPlanned_throwsBusinessException")
        void activate_whenSprintNotPlanned_throwsBusinessException() {
            sprint.setStatus(SprintStatus.COMPLETED);

            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.activate(TestDataFactory.SPRINT_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("PLANNED");
        }

        @Test
        @DisplayName("activate_whenNotFound_throwsResourceNotFoundException")
        void activate_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(sprintRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sprintService.activate(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sprint");
        }
    }

    // ------------------------------------------------------------------ complete

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("complete_whenSprintIsActive_setsStatusCompleted")
        void complete_whenSprintIsActive_setsStatusCompleted() {
            sprint.setStatus(SprintStatus.ACTIVE);

            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));
            when(sprintRepository.save(sprint)).thenReturn(sprint);
            when(sprintMapper.toDto(sprint)).thenReturn(sprintDto);

            SprintDto result = sprintService.complete(TestDataFactory.SPRINT_ID);

            assertThat(sprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("complete_whenSprintIsNotActive_throwsBusinessException")
        void complete_whenSprintIsNotActive_throwsBusinessException() {
            sprint.setStatus(SprintStatus.PLANNED);

            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.complete(TestDataFactory.SPRINT_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ACTIVE");

            verify(sprintRepository, never()).save(any());
        }

        @Test
        @DisplayName("complete_whenNotFound_throwsResourceNotFoundException")
        void complete_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(sprintRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sprintService.complete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sprint");
        }
    }

    // ------------------------------------------------------------------ delete

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("delete_whenPlannedSprint_deletesSuccessfully")
        void delete_whenPlannedSprint_deletesSuccessfully() {
            sprint.setStatus(SprintStatus.PLANNED);
            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));

            sprintService.delete(TestDataFactory.SPRINT_ID);

            verify(sprintRepository).delete(sprint);
        }

        @Test
        @DisplayName("delete_whenActiveSprint_throwsBusinessException")
        void delete_whenActiveSprint_throwsBusinessException() {
            sprint.setStatus(SprintStatus.ACTIVE);
            when(sprintRepository.findById(TestDataFactory.SPRINT_ID))
                    .thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.delete(TestDataFactory.SPRINT_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Active sprints cannot be deleted");

            verify(sprintRepository, never()).delete(any());
        }

        @Test
        @DisplayName("delete_whenNotFound_throwsResourceNotFoundException")
        void delete_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(sprintRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sprintService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sprint");
        }
    }
}

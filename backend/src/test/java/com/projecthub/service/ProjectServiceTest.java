package com.projecthub.service;

import com.projecthub.TestDataFactory;
import com.projecthub.dto.PageResponse;
import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.ProjectMapper;
import com.projecthub.repository.ProjectRepository;
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
@DisplayName("ProjectService unit tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectDto projectDto;

    @BeforeEach
    void setUp() {
        project    = TestDataFactory.createProject();
        projectDto = TestDataFactory.createProjectDto();
    }

    // ------------------------------------------------------------------ findAll (paginated)

    @Nested
    @DisplayName("findAll(Pageable)")
    class FindAllPageable {

        @Test
        @DisplayName("findAll_withExistingProjects_returnsPageResponse")
        void findAll_withExistingProjects_returnsPageResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

            when(projectRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            PageResponse<ProjectDto> result = projectService.findAll(pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).code()).isEqualTo("TEST-PRJ");
        }

        @Test
        @DisplayName("findAll_withEmptyRepository_returnsEmptyPage")
        void findAll_withEmptyRepository_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Project> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(projectRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(emptyPage);

            PageResponse<ProjectDto> result = projectService.findAll(pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    // ------------------------------------------------------------------ findById

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("findById_whenProjectExists_returnsProjectDto")
        void findById_whenProjectExists_returnsProjectDto() {
            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.findById(TestDataFactory.PROJECT_ID);

            assertThat(result.id()).isEqualTo(TestDataFactory.PROJECT_ID);
            assertThat(result.code()).isEqualTo("TEST-PRJ");
        }

        @Test
        @DisplayName("findById_whenNotFound_throwsResourceNotFoundException")
        void findById_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(projectRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.findById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project");
        }
    }

    // ------------------------------------------------------------------ create

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create_withValidRequest_savesAndReturnsDto")
        void create_withValidRequest_savesAndReturnsDto() {
            ProjectCreateRequest request = TestDataFactory.createProjectCreateRequest();

            when(projectRepository.existsByCode("TEST-PRJ")).thenReturn(false);
            when(projectMapper.toEntity(request)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.create(request);

            assertThat(result.code()).isEqualTo("TEST-PRJ");
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("create_withDuplicateCode_throwsBusinessException")
        void create_withDuplicateCode_throwsBusinessException() {
            ProjectCreateRequest request = TestDataFactory.createProjectCreateRequest();
            when(projectRepository.existsByCode("TEST-PRJ")).thenReturn(true);

            assertThatThrownBy(() -> projectService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("TEST-PRJ");

            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("create_withNullStatus_defaultsToPLANNING")
        void create_withNullStatus_defaultsToPLANNING() {
            ProjectCreateRequest request = new ProjectCreateRequest(
                    "NEW-CODE", "New Project", null, null, null, null, null);
            Project projectWithoutStatus = Project.builder()
                    .id(UUID.randomUUID())
                    .code("NEW-CODE")
                    .name("New Project")
                    .status(null)
                    .build();
            Project savedProject = Project.builder()
                    .id(UUID.randomUUID())
                    .code("NEW-CODE")
                    .name("New Project")
                    .status(ProjectStatus.PLANNING)
                    .build();
            ProjectDto savedDto = new ProjectDto(
                    savedProject.getId(), "NEW-CODE", "New Project",
                    null, null, null, null, ProjectStatus.PLANNING, null, null);

            when(projectRepository.existsByCode("NEW-CODE")).thenReturn(false);
            when(projectMapper.toEntity(request)).thenReturn(projectWithoutStatus);
            when(projectRepository.save(projectWithoutStatus)).thenReturn(savedProject);
            when(projectMapper.toDto(savedProject)).thenReturn(savedDto);

            ProjectDto result = projectService.create(request);

            assertThat(result.status()).isEqualTo(ProjectStatus.PLANNING);
            assertThat(projectWithoutStatus.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        }
    }

    // ------------------------------------------------------------------ update

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("update_whenProjectExists_updatesAndReturnsDto")
        void update_whenProjectExists_updatesAndReturnsDto() {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", "Updated desc", null, null, null, ProjectStatus.DEVELOPMENT);

            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.update(TestDataFactory.PROJECT_ID, request);

            assertThat(result).isNotNull();
            verify(projectMapper).updateEntity(request, project);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("update_whenNotFound_throwsResourceNotFoundException")
        void update_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Name", null, null, null, null, null);
            when(projectRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.update(unknownId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project");
        }
    }

    // ------------------------------------------------------------------ softDelete

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("softDelete_whenProjectExists_setsDeletedAtAndStatusClosed")
        void softDelete_whenProjectExists_setsDeletedAtAndStatusClosed() {
            when(projectRepository.findByIdAndDeletedAtIsNull(TestDataFactory.PROJECT_ID))
                    .thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);

            projectService.softDelete(TestDataFactory.PROJECT_ID);

            assertThat(project.getDeletedAt()).isNotNull();
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.CLOSED);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("softDelete_whenNotFound_throwsResourceNotFoundException")
        void softDelete_whenNotFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            when(projectRepository.findByIdAndDeletedAtIsNull(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.softDelete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project");

            verify(projectRepository, never()).save(any());
        }
    }
}

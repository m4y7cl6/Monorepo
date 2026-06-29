package com.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projecthub.TestDataFactory;
import com.projecthub.config.TestSecurityConfig;
import com.projecthub.dto.PageResponse;
import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProjectController integration tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    private ObjectMapper objectMapper;
    private ProjectDto projectDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        projectDto = TestDataFactory.createProjectDto();
    }

    // ------------------------------------------------------------------ GET /api/projects

    @Nested
    @DisplayName("GET /api/projects")
    class GetAll {

        @Test
        @WithMockUser
        @DisplayName("getAll_whenAuthenticated_returns200WithPageResponse")
        void getAll_whenAuthenticated_returns200WithPageResponse() throws Exception {
            PageResponse<ProjectDto> pageResponse = new PageResponse<>(
                    List.of(projectDto), 1L, 1, 0, 20, true, true);
            when(projectService.findAll(any())).thenReturn(pageResponse);

            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].code").value("TEST-PRJ"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("getAll_whenUnauthenticated_returns401")
        void getAll_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ GET /api/projects/{id}

    @Nested
    @DisplayName("GET /api/projects/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("getById_whenProjectExists_returns200WithDto")
        void getById_whenProjectExists_returns200WithDto() throws Exception {
            when(projectService.findById(TestDataFactory.PROJECT_ID)).thenReturn(projectDto);

            mockMvc.perform(get("/api/projects/{id}", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.PROJECT_ID.toString()))
                    .andExpect(jsonPath("$.code").value("TEST-PRJ"))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.status").value("PLANNING"));
        }

        @Test
        @WithMockUser
        @DisplayName("getById_whenNotFound_returns404")
        void getById_whenNotFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(projectService.findById(unknownId))
                    .thenThrow(new ResourceNotFoundException("Project", "id", unknownId));

            mockMvc.perform(get("/api/projects/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------------ POST /api/projects

    @Nested
    @DisplayName("POST /api/projects")
    class Create {

        private ProjectCreateRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = TestDataFactory.createProjectCreateRequest();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("create_withAdminRole_returns201WithCreatedDto")
        void create_withAdminRole_returns201WithCreatedDto() throws Exception {
            when(projectService.create(any())).thenReturn(projectDto);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("TEST-PRJ"));
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withPmRole_returns201")
        void create_withPmRole_returns201() throws Exception {
            when(projectService.create(any())).thenReturn(projectDto);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("create_withViewerRole_returns403Forbidden")
        void create_withViewerRole_returns403Forbidden() throws Exception {
            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());

            verify(projectService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("create_withBlankCode_returns422UnprocessableEntity")
        void create_withBlankCode_returns422UnprocessableEntity() throws Exception {
            // GlobalExceptionHandler maps MethodArgumentNotValidException -> 422
            ProjectCreateRequest invalidRequest = new ProjectCreateRequest(
                    "", "Some Name", null, null, null, null, null);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.fieldErrors.code").exists());
        }
    }

    // ------------------------------------------------------------------ PUT /api/projects/{id}

    @Nested
    @DisplayName("PUT /api/projects/{id}")
    class Update {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("update_withAdminRole_returns200WithUpdatedDto")
        void update_withAdminRole_returns200WithUpdatedDto() throws Exception {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", null, null,
                    LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 31),
                    ProjectStatus.DEVELOPMENT);

            when(projectService.update(eq(TestDataFactory.PROJECT_ID), any())).thenReturn(projectDto);

            mockMvc.perform(put("/api/projects/{id}", TestDataFactory.PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.PROJECT_ID.toString()));
        }

        @Test
        @WithMockUser(roles = "DEVELOPER")
        @DisplayName("update_withDeveloperRole_returns403Forbidden")
        void update_withDeveloperRole_returns403Forbidden() throws Exception {
            ProjectUpdateRequest request = new ProjectUpdateRequest(
                    "Updated Name", null, null, null, null, null);

            mockMvc.perform(put("/api/projects/{id}", TestDataFactory.PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------ DELETE /api/projects/{id}

    @Nested
    @DisplayName("DELETE /api/projects/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("delete_withAdminRole_returns204NoContent")
        void delete_withAdminRole_returns204NoContent() throws Exception {
            doNothing().when(projectService).softDelete(TestDataFactory.PROJECT_ID);

            mockMvc.perform(delete("/api/projects/{id}", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isNoContent());

            verify(projectService).softDelete(TestDataFactory.PROJECT_ID);
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("delete_withPmRole_returns403Forbidden")
        void delete_withPmRole_returns403Forbidden() throws Exception {
            mockMvc.perform(delete("/api/projects/{id}", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isForbidden());

            verify(projectService, never()).softDelete(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("delete_whenProjectNotFound_returns404")
        void delete_whenProjectNotFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Project", "id", unknownId))
                    .when(projectService).softDelete(unknownId);

            mockMvc.perform(delete("/api/projects/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }
}

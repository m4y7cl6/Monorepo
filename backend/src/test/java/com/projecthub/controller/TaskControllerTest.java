package com.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projecthub.TestDataFactory;
import com.projecthub.config.TestSecurityConfig;
import com.projecthub.dto.PageResponse;
import com.projecthub.dto.TaskCreateRequest;
import com.projecthub.dto.TaskDto;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.service.TaskService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
@DisplayName("TaskController integration tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private ObjectMapper objectMapper;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        taskDto = TestDataFactory.createTaskDto();
    }

    // ------------------------------------------------------------------ GET /api/tasks

    @Nested
    @DisplayName("GET /api/tasks")
    class GetAll {

        @Test
        @WithMockUser
        @DisplayName("getAll_whenAuthenticated_returns200WithPageResponse")
        void getAll_whenAuthenticated_returns200WithPageResponse() throws Exception {
            PageResponse<TaskDto> page = new PageResponse<>(
                    List.of(taskDto), 1L, 1, 0, 20, true, true);
            when(taskService.findAll(any())).thenReturn(page);

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].taskNo").value("TASK-001"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("getAll_whenUnauthenticated_returns401")
        void getAll_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ GET /api/projects/{id}/tasks

    @Nested
    @DisplayName("GET /api/projects/{projectId}/tasks")
    class GetByProject {

        @Test
        @WithMockUser
        @DisplayName("getByProject_whenAuthenticated_returns200WithPageResponse")
        void getByProject_whenAuthenticated_returns200WithPageResponse() throws Exception {
            PageResponse<TaskDto> page = new PageResponse<>(
                    List.of(taskDto), 1L, 1, 0, 20, true, true);
            when(taskService.findByProjectId(eq(TestDataFactory.PROJECT_ID), any())).thenReturn(page);

            mockMvc.perform(get("/api/projects/{projectId}/tasks", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].projectId")
                            .value(TestDataFactory.PROJECT_ID.toString()));
        }

        @Test
        @DisplayName("getByProject_whenUnauthenticated_returns401")
        void getByProject_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/projects/{projectId}/tasks", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ POST /api/tasks

    @Nested
    @DisplayName("POST /api/tasks")
    class Create {

        @Test
        @WithMockUser(roles = "DEVELOPER")
        @DisplayName("create_withDeveloperRole_returns201WithCreatedDto")
        void create_withDeveloperRole_returns201WithCreatedDto() throws Exception {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);
            when(taskService.create(any())).thenReturn(taskDto);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.taskNo").value("TASK-001"));
        }

        @Test
        @WithMockUser(roles = "LEADER")
        @DisplayName("create_withLeaderRole_returns201")
        void create_withLeaderRole_returns201() throws Exception {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);
            when(taskService.create(any())).thenReturn(taskDto);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("create_withViewerRole_returns403Forbidden")
        void create_withViewerRole_returns403Forbidden() throws Exception {
            TaskCreateRequest request = TestDataFactory.createTaskCreateRequest(TestDataFactory.PROJECT_ID);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(taskService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "DEVELOPER")
        @DisplayName("create_withBlankTaskNo_returns422UnprocessableEntity")
        void create_withBlankTaskNo_returns422UnprocessableEntity() throws Exception {
            // GlobalExceptionHandler maps MethodArgumentNotValidException -> 422
            TaskCreateRequest invalid = new TaskCreateRequest(
                    "", TestDataFactory.PROJECT_ID, null,
                    "Some title", null, null, null, null, null, null, null, null);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.fieldErrors.taskNo").exists());
        }
    }

    // ------------------------------------------------------------------ PATCH /api/tasks/{id}/status

    @Nested
    @DisplayName("PATCH /api/tasks/{id}/status")
    class UpdateStatus {

        @Test
        @WithMockUser
        @DisplayName("updateStatus_whenAuthenticated_returns200WithUpdatedDto")
        void updateStatus_whenAuthenticated_returns200WithUpdatedDto() throws Exception {
            when(taskService.updateStatus(TestDataFactory.TASK_ID, TaskStatus.IN_PROGRESS))
                    .thenReturn(taskDto);

            mockMvc.perform(patch("/api/tasks/{id}/status", TestDataFactory.TASK_ID)
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.TASK_ID.toString()));
        }

        @Test
        @WithMockUser
        @DisplayName("updateStatus_whenTaskNotFound_returns404")
        void updateStatus_whenTaskNotFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(taskService.updateStatus(unknownId, TaskStatus.DONE))
                    .thenThrow(new ResourceNotFoundException("Task", "id", unknownId));

            mockMvc.perform(patch("/api/tasks/{id}/status", unknownId)
                            .param("status", "DONE"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("updateStatus_whenUnauthenticated_returns401")
        void updateStatus_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}/status", TestDataFactory.TASK_ID)
                            .param("status", "DONE"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ DELETE /api/tasks/{id}

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "LEADER")
        @DisplayName("delete_withLeaderRole_returns204NoContent")
        void delete_withLeaderRole_returns204NoContent() throws Exception {
            doNothing().when(taskService).softDelete(TestDataFactory.TASK_ID);

            mockMvc.perform(delete("/api/tasks/{id}", TestDataFactory.TASK_ID))
                    .andExpect(status().isNoContent());

            verify(taskService).softDelete(TestDataFactory.TASK_ID);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("delete_withAdminRole_returns204NoContent")
        void delete_withAdminRole_returns204NoContent() throws Exception {
            doNothing().when(taskService).softDelete(TestDataFactory.TASK_ID);

            mockMvc.perform(delete("/api/tasks/{id}", TestDataFactory.TASK_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "DEVELOPER")
        @DisplayName("delete_withDeveloperRole_returns403Forbidden")
        void delete_withDeveloperRole_returns403Forbidden() throws Exception {
            mockMvc.perform(delete("/api/tasks/{id}", TestDataFactory.TASK_ID))
                    .andExpect(status().isForbidden());

            verify(taskService, never()).softDelete(any());
        }
    }
}

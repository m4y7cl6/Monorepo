package com.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projecthub.TestDataFactory;
import com.projecthub.config.TestSecurityConfig;
import com.projecthub.dto.SprintCreateRequest;
import com.projecthub.dto.SprintDto;
import com.projecthub.entity.enums.SprintStatus;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.service.SprintService;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SprintController.class)
@Import(TestSecurityConfig.class)
@DisplayName("SprintController integration tests")
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SprintService sprintService;

    private ObjectMapper objectMapper;
    private SprintDto sprintDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        sprintDto = TestDataFactory.createSprintDto();
    }

    // ------------------------------------------------------------------ GET /api/sprints

    @Nested
    @DisplayName("GET /api/sprints")
    class GetAll {

        @Test
        @WithMockUser
        @DisplayName("getAll_whenAuthenticated_returns200WithList")
        void getAll_whenAuthenticated_returns200WithList() throws Exception {
            when(sprintService.findAll()).thenReturn(List.of(sprintDto));

            mockMvc.perform(get("/api/sprints"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(TestDataFactory.SPRINT_ID.toString()));
        }

        @Test
        @DisplayName("getAll_whenUnauthenticated_returns401")
        void getAll_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/sprints"))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * API contract test: response array elements must use the field name "name",
         * not the legacy "sprintName" that was present before the bug fix.
         * Regression guard for the bug where SprintDto used sprintName and the
         * frontend received an empty display name.
         */
        @Test
        @WithMockUser
        @DisplayName("getAll_responseContainsNameNotSprintName")
        void getAll_responseContainsNameNotSprintName() throws Exception {
            when(sprintService.findAll()).thenReturn(List.of(sprintDto));

            mockMvc.perform(get("/api/sprints"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Sprint 1"))
                    .andExpect(jsonPath("$[0].sprintName").doesNotExist());
        }
    }

    // ------------------------------------------------------------------ GET /api/sprints/{id}

    @Nested
    @DisplayName("GET /api/sprints/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("getById_whenSprintExists_returns200WithDto")
        void getById_whenSprintExists_returns200WithDto() throws Exception {
            when(sprintService.findById(TestDataFactory.SPRINT_ID)).thenReturn(sprintDto);

            mockMvc.perform(get("/api/sprints/{id}", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.SPRINT_ID.toString()))
                    .andExpect(jsonPath("$.name").value("Sprint 1"))
                    .andExpect(jsonPath("$.status").value("PLANNED"));
        }

        @Test
        @WithMockUser
        @DisplayName("getById_whenNotFound_returns404")
        void getById_whenNotFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(sprintService.findById(unknownId))
                    .thenThrow(new ResourceNotFoundException("Sprint", "id", unknownId));

            mockMvc.perform(get("/api/sprints/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("getById_whenUnauthenticated_returns401")
        void getById_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/sprints/{id}", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ GET /api/projects/{projectId}/sprints

    @Nested
    @DisplayName("GET /api/projects/{projectId}/sprints")
    class GetByProject {

        @Test
        @WithMockUser
        @DisplayName("getByProject_whenAuthenticated_returns200WithList")
        void getByProject_whenAuthenticated_returns200WithList() throws Exception {
            when(sprintService.findByProjectId(TestDataFactory.PROJECT_ID))
                    .thenReturn(List.of(sprintDto));

            mockMvc.perform(get("/api/projects/{projectId}/sprints", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Sprint 1"));
        }

        @Test
        @DisplayName("getByProject_whenUnauthenticated_returns401")
        void getByProject_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/projects/{projectId}/sprints", TestDataFactory.PROJECT_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------ POST /api/sprints

    @Nested
    @DisplayName("POST /api/sprints")
    class Create {

        private SprintCreateRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID,
                    "Sprint 1",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 14),
                    "Deliver MVP features",
                    SprintStatus.PLANNED
            );
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withPmRole_returns201")
        void create_withPmRole_returns201() throws Exception {
            when(sprintService.create(any())).thenReturn(sprintDto);

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("create_withAdminRole_returns201WithCreatedDto")
        void create_withAdminRole_returns201WithCreatedDto() throws Exception {
            when(sprintService.create(any())).thenReturn(sprintDto);

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.SPRINT_ID.toString()));
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("create_withViewerRole_returns403Forbidden")
        void create_withViewerRole_returns403Forbidden() throws Exception {
            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());

            verify(sprintService, never()).create(any());
        }

        /**
         * API contract test: the request body must use the field "name" for the sprint name.
         * This is the corrected field name after the bug fix that changed "sprintName" -> "name"
         * in SprintCreateRequest. If the contract is correct, a body with "name" is accepted
         * and the response also serialises the sprint name under "name".
         */
        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withNameField_returns201AndResponseContainsName")
        void create_withNameField_returns201AndResponseContainsName() throws Exception {
            when(sprintService.create(any())).thenReturn(sprintDto);

            // Build JSON explicitly to verify the wire format uses "name", not "sprintName"
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "projectId", TestDataFactory.PROJECT_ID.toString(),
                    "name", "Sprint 1",
                    "startDate", "2026-01-01",
                    "endDate", "2026-01-14",
                    "goal", "Deliver MVP features",
                    "status", "PLANNED"
            ));

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Sprint 1"))
                    .andExpect(jsonPath("$.sprintName").doesNotExist());
        }

        /**
         * API contract test: a request body that uses the legacy field "sprintName" instead of
         * "name" must be rejected with 422 Unprocessable Entity, because "name" will be null
         * and @NotBlank will fail validation. This is the exact bug scenario — old frontend
         * code sending "sprintName" causes a validation error rather than silently creating
         * a sprint with a blank name.
         */
        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withSprintNameFieldInsteadOfName_returns422")
        void create_withSprintNameFieldInsteadOfName_returns422() throws Exception {
            // Deliberately send the legacy "sprintName" key; "name" will be absent (null)
            String legacyRequestBody = objectMapper.writeValueAsString(Map.of(
                    "projectId", TestDataFactory.PROJECT_ID.toString(),
                    "sprintName", "Sprint 1",
                    "startDate", "2026-01-01",
                    "endDate", "2026-01-14",
                    "goal", "Deliver MVP features",
                    "status", "PLANNED"
            ));

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(legacyRequestBody))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.fieldErrors.name").exists());
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withBlankName_returns422WithFieldError")
        void create_withBlankName_returns422WithFieldError() throws Exception {
            SprintCreateRequest invalidRequest = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID,
                    "",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 14),
                    null,
                    null
            );

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.fieldErrors.name").exists());
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("create_withNullProjectId_returns422WithFieldError")
        void create_withNullProjectId_returns422WithFieldError() throws Exception {
            // projectId is null — must fail @NotNull
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "name", "Sprint 1"
            ));

            mockMvc.perform(post("/api/sprints")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.fieldErrors.projectId").exists());
        }
    }

    // ------------------------------------------------------------------ PUT /api/sprints/{id}

    @Nested
    @DisplayName("PUT /api/sprints/{id}")
    class Update {

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("update_withPmRole_returns200WithUpdatedDto")
        void update_withPmRole_returns200WithUpdatedDto() throws Exception {
            SprintCreateRequest request = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID,
                    "Sprint 1 Updated",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 21),
                    "Updated goal",
                    SprintStatus.PLANNED
            );
            when(sprintService.update(eq(TestDataFactory.SPRINT_ID), any())).thenReturn(sprintDto);

            mockMvc.perform(put("/api/sprints/{id}", TestDataFactory.SPRINT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.SPRINT_ID.toString()));
        }

        @Test
        @WithMockUser(roles = "DEVELOPER")
        @DisplayName("update_withDeveloperRole_returns403Forbidden")
        void update_withDeveloperRole_returns403Forbidden() throws Exception {
            SprintCreateRequest request = new SprintCreateRequest(
                    TestDataFactory.PROJECT_ID,
                    "Sprint 1",
                    null, null, null, null
            );

            mockMvc.perform(put("/api/sprints/{id}", TestDataFactory.SPRINT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------ POST /api/sprints/{id}/activate

    @Nested
    @DisplayName("POST /api/sprints/{id}/activate")
    class Activate {

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("activate_withPmRole_returns200")
        void activate_withPmRole_returns200() throws Exception {
            when(sprintService.activate(TestDataFactory.SPRINT_ID)).thenReturn(sprintDto);

            mockMvc.perform(post("/api/sprints/{id}/activate", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TestDataFactory.SPRINT_ID.toString()));
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("activate_withViewerRole_returns403")
        void activate_withViewerRole_returns403() throws Exception {
            mockMvc.perform(post("/api/sprints/{id}/activate", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------ POST /api/sprints/{id}/complete

    @Nested
    @DisplayName("POST /api/sprints/{id}/complete")
    class Complete {

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("complete_withPmRole_returns200")
        void complete_withPmRole_returns200() throws Exception {
            when(sprintService.complete(TestDataFactory.SPRINT_ID)).thenReturn(sprintDto);

            mockMvc.perform(post("/api/sprints/{id}/complete", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("complete_withViewerRole_returns403")
        void complete_withViewerRole_returns403() throws Exception {
            mockMvc.perform(post("/api/sprints/{id}/complete", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isForbidden());
        }
    }

    // ------------------------------------------------------------------ DELETE /api/sprints/{id}

    @Nested
    @DisplayName("DELETE /api/sprints/{id}")
    class Delete {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("delete_withAdminRole_returns204NoContent")
        void delete_withAdminRole_returns204NoContent() throws Exception {
            doNothing().when(sprintService).delete(TestDataFactory.SPRINT_ID);

            mockMvc.perform(delete("/api/sprints/{id}", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isNoContent());

            verify(sprintService).delete(TestDataFactory.SPRINT_ID);
        }

        @Test
        @WithMockUser(roles = "PM")
        @DisplayName("delete_withPmRole_returns204NoContent")
        void delete_withPmRole_returns204NoContent() throws Exception {
            doNothing().when(sprintService).delete(TestDataFactory.SPRINT_ID);

            mockMvc.perform(delete("/api/sprints/{id}", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "LEADER")
        @DisplayName("delete_withLeaderRole_returns403Forbidden")
        void delete_withLeaderRole_returns403Forbidden() throws Exception {
            // DELETE only allows ADMIN and PM, not LEADER
            mockMvc.perform(delete("/api/sprints/{id}", TestDataFactory.SPRINT_ID))
                    .andExpect(status().isForbidden());

            verify(sprintService, never()).delete(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("delete_whenNotFound_returns404")
        void delete_whenNotFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Sprint", "id", unknownId))
                    .when(sprintService).delete(unknownId);

            mockMvc.perform(delete("/api/sprints/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }
}

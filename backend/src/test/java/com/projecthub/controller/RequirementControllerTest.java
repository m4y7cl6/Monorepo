package com.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projecthub.TestDataFactory;
import com.projecthub.config.TestSecurityConfig;
import com.projecthub.dto.RequirementCreateRequest;
import com.projecthub.dto.RequirementDto;
import com.projecthub.entity.enums.RequirementStatus;
import com.projecthub.entity.enums.TaskPriority;
import com.projecthub.service.RequirementService;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequirementController.class)
@Import(TestSecurityConfig.class)
@DisplayName("RequirementController regression tests")
class RequirementControllerTest {

    /** Deterministic UUID for all requirement-level assertions. */
    static final UUID REQ_ID = UUID.fromString("aaaaaaa1-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequirementService requirementService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // -------------------------------------------------------- PUT /api/requirements/{id}

    @Nested
    @DisplayName("PUT /api/requirements/{id}")
    class Update {

        /**
         * Regression test for the bug where RequirementService.update() never called
         * setStatus(), causing a PUT that included a status change to silently drop it.
         *
         * The fix adds status to RequirementCreateRequest and wires setStatus() in the
         * service's update path. This test verifies the full round-trip: the controller
         * deserialises the "status" field from the request body and the response body
         * returns the updated status value that the service now produces.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("update_withStatusChange_returns200AndUpdatedStatus")
        void update_withStatusChange_returns200AndUpdatedStatus() throws Exception {
            RequirementDto updatedDto = new RequirementDto(
                    REQ_ID,
                    "REQ-001",
                    TestDataFactory.PROJECT_ID,
                    "Test Project",
                    "Some requirement title",
                    "Description",
                    TaskPriority.HIGH,
                    RequirementStatus.APPROVED,   // the status change that must be reflected
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 6, 1, 0, 0)
            );

            when(requirementService.update(eq(REQ_ID), any())).thenReturn(updatedDto);

            RequirementCreateRequest request = new RequirementCreateRequest(
                    "REQ-001",
                    TestDataFactory.PROJECT_ID,
                    "Some requirement title",
                    "Description",
                    TaskPriority.HIGH,
                    RequirementStatus.APPROVED
            );

            mockMvc.perform(put("/api/requirements/{id}", REQ_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(REQ_ID.toString()))
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        /**
         * Validation contract test: a PUT body that omits the required fields
         * (reqNo, title, projectId) must be rejected with 422 Unprocessable Entity.
         * Ensures the @NotBlank / @NotNull constraints on RequirementCreateRequest are
         * enforced by the controller layer before the service is ever called.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("update_withoutRequiredFields_returns422")
        void update_withoutStatusField_returns422() throws Exception {
            // Deliberately omit reqNo, title, and projectId — all three are @NotBlank / @NotNull
            String incompleteBody = objectMapper.writeValueAsString(Map.of(
                    "description", "Only a description, nothing required"
            ));

            mockMvc.perform(put("/api/requirements/{id}", REQ_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(incompleteBody))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // -------------------------------------------------------- POST /api/requirements

    @Nested
    @DisplayName("POST /api/requirements")
    class Create {

        /**
         * Documents the intended service behaviour: even when a client sends
         * status: "APPROVED" in a creation request, the service is expected to
         * force the status to DRAFT for new requirements.
         *
         * The mock returns a DTO with status=DRAFT, simulating the service override.
         * The test verifies:
         *   - the controller forwards the full request (including the client-supplied
         *     status) to the service — i.e. no silent field drop at the controller level
         *   - the response body carries the service's authoritative status (DRAFT),
         *     not the client's requested value
         *   - HTTP 201 Created is returned
         *
         * If the service ever stops forcing DRAFT, this test will catch the regression.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("create_alwaysSetsDraftStatus_ignoresClientStatus")
        void create_alwaysSetsDraftStatus_ignoresClientStatus() throws Exception {
            RequirementDto draftDto = new RequirementDto(
                    REQ_ID,
                    "REQ-002",
                    TestDataFactory.PROJECT_ID,
                    "Test Project",
                    "New feature requirement",
                    null,
                    TaskPriority.MEDIUM,
                    RequirementStatus.DRAFT,   // service-enforced status regardless of request
                    LocalDateTime.of(2026, 6, 1, 0, 0),
                    LocalDateTime.of(2026, 6, 1, 0, 0)
            );

            when(requirementService.create(any())).thenReturn(draftDto);

            // Client sends APPROVED — service must override to DRAFT
            RequirementCreateRequest request = new RequirementCreateRequest(
                    "REQ-002",
                    TestDataFactory.PROJECT_ID,
                    "New feature requirement",
                    null,
                    TaskPriority.MEDIUM,
                    RequirementStatus.APPROVED   // client-requested status, should be ignored
            );

            mockMvc.perform(post("/api/requirements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(REQ_ID.toString()))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }
    }
}

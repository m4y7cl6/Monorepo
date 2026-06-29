package com.projecthub.controller;

import com.projecthub.config.TestSecurityConfig;
import com.projecthub.dto.DashboardSummaryDto;
import com.projecthub.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(TestSecurityConfig.class)
@DisplayName("DashboardController integration tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private DashboardSummaryDto buildSummaryDto() {
        return new DashboardSummaryDto(
                10L,                        // totalProjects
                8L,                         // activeProjects
                50L,                        // totalTasks
                10L,                        // backlogTasks
                8L,                         // openTasks
                15L,                        // inProgressTasks
                5L,                         // reviewTasks
                2L,                         // testingTasks
                5L,                         // doneTasks
                8L,                         // totalBugs
                3L,                         // openBugs
                1L,                         // criticalBugs
                12L,                        // totalRequirements
                4L,                         // approvedRequirements
                30L,                        // totalWorklogs
                new BigDecimal("120.50")    // totalHoursLogged
        );
    }

    // ------------------------------------------------------------------ GET /api/dashboard/summary

    @Nested
    @DisplayName("GET /api/dashboard/summary")
    class GetSummary {

        @Test
        @WithMockUser
        @DisplayName("getSummary_whenAuthenticated_returns200WithFlatStructure")
        void getSummary_whenAuthenticated_returns200WithFlatStructure() throws Exception {
            when(dashboardService.getSummary()).thenReturn(buildSummaryDto());

            mockMvc.perform(get("/api/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalProjects").value(10))
                    .andExpect(jsonPath("$.activeProjects").value(8))
                    .andExpect(jsonPath("$.totalTasks").value(50))
                    .andExpect(jsonPath("$.backlogTasks").value(10))
                    .andExpect(jsonPath("$.inProgressTasks").value(15))
                    .andExpect(jsonPath("$.reviewTasks").value(5))
                    .andExpect(jsonPath("$.testingTasks").value(2))
                    .andExpect(jsonPath("$.doneTasks").value(5))
                    .andExpect(jsonPath("$.totalBugs").value(8))
                    .andExpect(jsonPath("$.openBugs").value(3))
                    .andExpect(jsonPath("$.criticalBugs").value(1))
                    .andExpect(jsonPath("$.totalRequirements").value(12))
                    .andExpect(jsonPath("$.approvedRequirements").value(4))
                    .andExpect(jsonPath("$.totalHoursLogged").value(120.50));
        }

        @Test
        @DisplayName("getSummary_whenUnauthenticated_returns401")
        void getSummary_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/dashboard/summary"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("getSummary_returnsContentTypeJson")
        void getSummary_returnsContentTypeJson() throws Exception {
            when(dashboardService.getSummary()).thenReturn(buildSummaryDto());

            mockMvc.perform(get("/api/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"));
        }
    }
}

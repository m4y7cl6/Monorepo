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
import java.util.Map;

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
        DashboardSummaryDto.ProjectSummary projects =
                new DashboardSummaryDto.ProjectSummary(10, 3, 4, 1, 1, 1, 0);

        DashboardSummaryDto.TaskSummary tasks =
                new DashboardSummaryDto.TaskSummary(
                        50, 10, 8, 15, 7, 5, 5,
                        Map.of("TEST-PRJ", 20L));

        DashboardSummaryDto.BugSummary bugs =
                new DashboardSummaryDto.BugSummary(8, 3, 2, 2, 1, 1, 2);

        DashboardSummaryDto.WorklogSummary worklog =
                new DashboardSummaryDto.WorklogSummary(
                        new BigDecimal("120.50"),
                        new BigDecimal("480.00"),
                        12L);

        return new DashboardSummaryDto(projects, tasks, bugs, worklog);
    }

    // ------------------------------------------------------------------ GET /api/dashboard/summary

    @Nested
    @DisplayName("GET /api/dashboard/summary")
    class GetSummary {

        @Test
        @WithMockUser
        @DisplayName("getSummary_whenAuthenticated_returns200WithCorrectStructure")
        void getSummary_whenAuthenticated_returns200WithCorrectStructure() throws Exception {
            when(dashboardService.getSummary()).thenReturn(buildSummaryDto());

            mockMvc.perform(get("/api/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectSummary").exists())
                    .andExpect(jsonPath("$.projectSummary.total").value(10))
                    .andExpect(jsonPath("$.projectSummary.planning").value(3))
                    .andExpect(jsonPath("$.projectSummary.development").value(4))
                    .andExpect(jsonPath("$.taskSummary").exists())
                    .andExpect(jsonPath("$.taskSummary.total").value(50))
                    .andExpect(jsonPath("$.taskSummary.inProgress").value(15))
                    .andExpect(jsonPath("$.taskSummary.done").value(5))
                    .andExpect(jsonPath("$.bugSummary").exists())
                    .andExpect(jsonPath("$.bugSummary.total").value(8))
                    .andExpect(jsonPath("$.bugSummary.critical").value(1))
                    .andExpect(jsonPath("$.worklogSummary").exists())
                    .andExpect(jsonPath("$.worklogSummary.totalHoursThisWeek").value(120.50))
                    .andExpect(jsonPath("$.worklogSummary.activeContributors").value(12));
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

        @Test
        @WithMockUser
        @DisplayName("getSummary_byProjectMapIncludesExpectedProjectCode")
        void getSummary_byProjectMapIncludesExpectedProjectCode() throws Exception {
            when(dashboardService.getSummary()).thenReturn(buildSummaryDto());

            mockMvc.perform(get("/api/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskSummary.byProject['TEST-PRJ']").value(20));
        }
    }
}

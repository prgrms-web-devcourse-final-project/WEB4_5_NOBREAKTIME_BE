package com.mallang.mallang_backend.domain.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.dashboard.dto.*;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Import(DashboardControllerTest.TestConfig.class)
@DisplayName("DashboardController 통합 테스트")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardService dashboardService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CustomUserDetails user;

    @BeforeEach
    void setup() {
        user = new CustomUserDetails(1L, "ROLE_STANDARD");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities())
        );
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("대시보드 통계를 조회할 수 있다")
    void statistics_success() throws Exception {
        StatisticResponse mockResponse = new StatisticResponse("nickname", 10, new DailyGoal(3, 5, 60.0, new AchievementDetail(2, 3)), new LevelStatus("A", "B", LocalDate.now().atStartOfDay(), false));
        when(dashboardService.getStatistics(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/dashboard/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.userName").value("nickname"));
    }

    @Test
    @DisplayName("학습 목표를 수정할 수 있다")
    void updateGoal_success() throws Exception {
        UpdateGoalRequest request = new UpdateGoalRequest(3, 5);

        mockMvc.perform(patch("/api/v1/dashboard/goal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("학습 목표가 설정되었습니다."));
    }

    @Test
    @DisplayName("기간별 학습 통계를 조회할 수 있다")
    void getCalendarData_success() throws Exception {
        LearningHistoryResponse response = new LearningHistoryResponse(
                new LearningHistory("00:10:00", 3, 1, 1),
                new LearningHistory("00:05:00", 2, 0, 0),
                new LearningHistory("00:15:00", 5, 1, 1)
        );
        when(dashboardService.getLearningStatisticsByPeriod(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.week.quizCount").value(5));
    }

    @Test
    @DisplayName("학습 레벨을 측정할 수 있다")
    void levelCheck_success() throws Exception {
        LevelCheckResponse response = new LevelCheckResponse("A", "B");
        when(dashboardService.checkLevel(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/dashboard/level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wordLevel").value("A"))
                .andExpect(jsonPath("$.data.expressionLevel").value("B"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DashboardService dashboardService() {
            return mock(DashboardService.class);
        }
    }
}

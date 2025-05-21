package com.mallang.mallang_backend.domain.sentence.expressionbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.*;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@Import(ExpressionBookControllerTest.TestConfig.class)
@SpringBootTest
@DisplayName("ExpressionBookController 단위 테스트")
class ExpressionBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpressionBookService expressionBookService;

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
    @DisplayName("추가 표현함 생성")
    void createExpressionBook() throws Exception {
        ExpressionBookRequest request = new ExpressionBookRequest("My Expressions");
        when(expressionBookService.create(any(), any())).thenReturn(10L);

        mockMvc.perform(post("/api/v1/expressionbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("user", user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("추가 표현함이 생성되었습니다."))
                .andExpect(jsonPath("$.data").value(10L));
    }

    @Test
    @DisplayName("표현함 목록 조회")
    void getAllExpressionBooks() throws Exception {
        when(expressionBookService.getByMember(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/expressionbooks")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현함 목록 조회에 성공했습니다."));
    }

    @Test
    @DisplayName("표현함 이름 수정")
    void updateExpressionBookName() throws Exception {
        UpdateExpressionBookNameRequest request = new UpdateExpressionBookNameRequest("Updated Name");
        doNothing().when(expressionBookService).updateName(any(), any(), any());

        mockMvc.perform(patch("/api/v1/expressionbooks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현함 이름이 수정되었습니다."));
    }

    @Test
    @DisplayName("표현함 삭제")
    void deleteExpressionBook() throws Exception {
        doNothing().when(expressionBookService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/expressionbooks/1")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현함이 삭제되었습니다."));
    }

    @Test
    @DisplayName("여러 표현함의 표현 목록 조회")
    void getExpressionsByBooks() throws Exception {
        when(expressionBookService.getExpressionsByBook(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/expressionbooks/view")
                        .param("expressionBookIds", "1", "2")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현함의 표현 목록 조회에 성공했습니다."));
    }

    @Test
    @DisplayName("표현함에 표현 저장")
    void saveExpression() throws Exception {
        ExpressionSaveRequest request = new ExpressionSaveRequest("video123", 100L);
        doNothing().when(expressionBookService).save(any(), any(), any());

        mockMvc.perform(post("/api/v1/expressionbooks/1/expressions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현함에 표현이 저장되었습니다."));
    }

    @Test
    @DisplayName("표현 삭제")
    void deleteExpressions() throws Exception {
        DeleteExpressionsRequest request = new DeleteExpressionsRequest(1L, List.of(101L, 102L));
        doNothing().when(expressionBookService).deleteExpressionsFromExpressionBook(any(), any());

        mockMvc.perform(post("/api/v1/expressionbooks/expressions/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현이 표현함에서 삭제되었습니다."));
    }

    @Test
    @DisplayName("표현 이동")
    void moveExpressions() throws Exception {
        MoveExpressionsRequest request = new MoveExpressionsRequest(1L, 2L, List.of(101L));
        doNothing().when(expressionBookService).moveExpressions(any(), any());

        mockMvc.perform(patch("/api/v1/expressionbooks/expressions/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현이 다른 표현함으로 이동되었습니다."));
    }

    @Test
    @DisplayName("표현 검색")
    void searchExpressions() throws Exception {
        when(expressionBookService.searchExpressions(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/expressionbooks/search")
                        .param("keyword", "test")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("표현 검색 결과입니다."));
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Bean
        public ExpressionBookService expressionBookService() {
            return mock(ExpressionBookService.class);
        }
    }
}

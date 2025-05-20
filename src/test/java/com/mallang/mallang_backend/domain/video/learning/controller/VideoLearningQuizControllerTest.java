package com.mallang.mallang_backend.domain.video.learning.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningExpressionQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.dto.VideoLearningWordQuizListResponse;
import com.mallang.mallang_backend.domain.video.learning.service.VideoLearningQuizService;
import com.mallang.mallang_backend.testfactory.VideoLearningTestFactory;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Security/필터 비활성화
class VideoLearningQuizControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VideoLearningQuizService videoLearningQuizService;

	@BeforeEach
	void setUp() {
		// -------- 단어 퀴즈 더미 생성 --------
		VideoLearningWordQuizListResponse dummyWordResp =
			VideoLearningTestFactory.createWordQuizListResponse(
				List.of(
					VideoLearningTestFactory.createWordQuizItem(
						1L,
						"00:00:01",   // startTime
						"00:00:03",   // endTime
						"Speaker",    // speaker
						"apple",      // word
						"사과",        // meaning
						"I have an {}",            // sentence (빈칸)
						"나는 사과 한 개를 가지고 있다" // sentenceMeaning
					)
				)
			);
		Mockito.when(videoLearningQuizService.makeQuizList("12345"))
			.thenReturn(dummyWordResp);

		// -------- 표현 퀴즈 더미 생성 --------
		VideoLearningExpressionQuizListResponse dummyExprResp =
			VideoLearningTestFactory.createExpressionQuizListResponse(
				List.of(
					VideoLearningTestFactory.createExpressionQuizItem(
						"{} is tasty",                 // question
						"Pizza is tasty",              // original
						List.of("Pizza", "Burger", "Sushi"), // choices
						"피자는 맛있다"                 // meaning
					)
				)
			);
		Mockito.when(videoLearningQuizService.makeExpressionQuizList("67890"))
			.thenReturn(dummyExprResp);
	}

	@Test
	@DisplayName("단어 퀴즈 조회 - 성공")
	void getWordsQuiz_success() throws Exception {
		mockMvc.perform(get("/api/v1/videos/12345/quiz/words"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.data.quiz[0].word").value("apple"));
	}

	@Test
	@DisplayName("표현 퀴즈 조회 - 성공")
	void getExpressionsQuiz_success() throws Exception {
		mockMvc.perform(get("/api/v1/videos/67890/quiz/expressions"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.data.quiz[0].original").value("Pizza is tasty"));
	}
}

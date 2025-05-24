package com.mallang.mallang_backend.domain.videohistory.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import com.mallang.mallang_backend.global.exception.message.MessageService;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
class VideoHistoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VideoHistoryService historyService;

	@MockitoBean
	private MessageService messageService;

	private CustomUserDetails userDetail;

	@BeforeEach
	void setUp() {
		userDetail = Mockito.mock(CustomUserDetails.class);
		Mockito.when(userDetail.getMemberId()).thenReturn(1L);
		var auth = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	@DisplayName("최근 5개 시청 기록 조회 성공")
	void getRecentVideos_success() throws Exception {
		VideoHistoryResponse resp = new VideoHistoryResponse(
			"video-123",
			"Some Title",
			"thumb.jpg",
			LocalDateTime.of(2025,5,24,10,0),
			"02:30"
		);

		given(historyService.getRecentHistories(1L))
			.willReturn(List.of(resp));

		mockMvc.perform(get("/api/v1/videohistory/videos/summary"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].videoId").value("video-123"))
			.andExpect(jsonPath("$.data[0].duration").value("02:30"));
	}

	@Test
	@DisplayName("전체 시청 기록 조회 성공")
	void getFullHistory_success() throws Exception {
		VideoHistoryResponse resp = new VideoHistoryResponse(
			"video-xyz",
			"Other Title",
			"thumb2.jpg",
			LocalDateTime.of(2025,5,24,11,0),
			"1:02:03"
		);

		given(historyService.getAllHistories(1L))
			.willReturn(List.of(resp));

		mockMvc.perform(get("/api/v1/videohistory/videos/history"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].title").value("Other Title"))
			.andExpect(jsonPath("$.data[0].duration").value("1:02:03"));
	}
}

package com.mallang.mallang_backend.domain.video.video.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mallang.mallang_backend.domain.video.video.VideoTestFactory;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.YoutubeCategoryId;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.LoginUserArgumentResolver;
import com.mallang.mallang_backend.global.util.sse.SseEmitterManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VideoControllerTest {

	@Mock
	VideoService videoService;
	@Mock SseEmitterManager sseEmitterManager;
	@Mock LoginUserArgumentResolver loginUserArgumentResolver;

	private VideoController controller;
	private MockMvc mockMvc;
	private MockedStatic<YoutubeCategoryId> youtubeCategoryIdMock;

	@BeforeEach
	void setUp() {
		// 컨트롤러 + MockMvc 세팅 (커스텀 리졸버 등록)
		controller = new VideoController(videoService, sseEmitterManager);
		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setCustomArgumentResolvers(loginUserArgumentResolver)
			.build();

		// @Login 파라미터용 리졸버 스텁
		CustomUserDetails userDetail = Mockito.mock(CustomUserDetails.class);
		// CustomUserDetails 파라미터에만 true
		given(loginUserArgumentResolver.supportsParameter(
			argThat(p -> p != null && p.getParameterType().equals(CustomUserDetails.class))
		)).willReturn(true);

		// 그 외에는 false
		given(loginUserArgumentResolver.supportsParameter(
			argThat(p -> p == null || !p.getParameterType().equals(CustomUserDetails.class))
		)).willReturn(false);
		given(loginUserArgumentResolver.resolveArgument(
			any(), any(), any(), any()
		)).willReturn(userDetail);

		// YoutubeCategoryId.of("cat") → EDUCATION
		youtubeCategoryIdMock = Mockito.mockStatic(YoutubeCategoryId.class);
		youtubeCategoryIdMock.when(() -> YoutubeCategoryId.of("cat"))
			.thenReturn(YoutubeCategoryId.EDUCATION);

		// SSE Emitter 스텁
		SseEmitter emitter = new SseEmitter(0L);
		given(sseEmitterManager.createEmitter(anyString()))
			.willReturn(emitter);
	}

	@AfterEach
	void tearDown() {
		youtubeCategoryIdMock.close();
	}

	@Test
	@DisplayName("GET /api/v1/videos/list - 정상 호출")
	void testGetVideoList() throws Exception {
		// given
		Videos v = VideoTestFactory.create("vid2", "제목2", Language.ENGLISH);
		VideoResponse resp = VideoResponse.from(v, true, "PT10M");
		given(videoService.getVideosForMember(
			anyString(),           // q
			anyString(),           // categoryId
			anyLong(),             // maxResults
			any(Long.class)        // memberId
		)).willReturn(List.of(resp));

		// when & then
		mockMvc.perform(get("/api/v1/videos/list")
				.param("q", "q")
				.param("category", "cat")
				.param("maxResults", "3")
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].videoId").value("vid2"))
			.andExpect(jsonPath("$.data[0].bookmarked").value(true))
			.andExpect(jsonPath("$.data[0].duration").value("10:00"));
	}

	@Test
	@DisplayName("GET /api/v1/videos/{id}/analysis - SSE INIT 이벤트")
	void testVideoAnalysisInitEvent() throws Exception {
		// videoService.analyzeWithSseAsync 에 대한 스텁
		willDoNothing().given(videoService)
			.analyzeWithSseAsync(any(), anyString(), anyString());

		// when & then
		mockMvc.perform(get("/api/v1/videos/XYZ/analysis")
				.accept("text/event-stream")
			)
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Type", "text/event-stream"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("event:INIT")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("data:")));
	}
}

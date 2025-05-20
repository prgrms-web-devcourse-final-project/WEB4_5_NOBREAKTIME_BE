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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mallang.mallang_backend.domain.video.video.VideoTestFactory;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.YoutubeCategoryId;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.config.WebConfig;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.LoginUserArgumentResolver;
import com.mallang.mallang_backend.global.util.sse.SseEmitterManager;

@SpringBootTest(
	classes = {
		VideoController.class,
		WebConfig.class,
		LoginUserArgumentResolver.class
	}
)
@ImportAutoConfiguration({
	JacksonAutoConfiguration.class,
	HttpMessageConvertersAutoConfiguration.class,
	WebMvcAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class VideoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VideoService videoService;

	@MockBean
	private LoginUserArgumentResolver loginUserArgumentResolver;

	@MockBean
	private SseEmitterManager sseEmitterManager;

	private CustomUserDetails userDetail;
	private MockedStatic<YoutubeCategoryId> youtubeCategoryIdMock;
	private SseEmitter emitter;

	@BeforeEach
	void setUp() {
		// 사용자 인증 정보 세팅
		userDetail = Mockito.mock(CustomUserDetails.class);
		given(userDetail.getMemberId()).willReturn(1L);
		given(userDetail.getAuthorities()).willReturn(List.of());
		var auth = new UsernamePasswordAuthenticationToken(
			userDetail, null, userDetail.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// @LoginUser 리졸버 스텁
		given(loginUserArgumentResolver.supportsParameter(any())).willReturn(true);
		given(loginUserArgumentResolver.resolveArgument(
			any(), any(), any(), any()
		)).willReturn(userDetail);

		// YoutubeCategoryId.of("cat") 스태틱 메서드 스텁
		youtubeCategoryIdMock = Mockito.mockStatic(YoutubeCategoryId.class);
		youtubeCategoryIdMock.when(() -> YoutubeCategoryId.of("cat"))
			.thenReturn(YoutubeCategoryId.EDUCATION);

		// SseEmitterManager.createEmitter(...) 가 non-null SseEmitter 반환하도록 스텁
		emitter = new SseEmitter(0L);
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
		// Given
		Videos v = VideoTestFactory.create("vid2", "제목2", Language.ENGLISH);
		VideoResponse resp = VideoResponse.from(v, true);

		given(videoService.getVideosForMember(
			anyString(), anyString(), anyLong(), anyLong()
		)).willReturn(List.of(resp));

		// When & Then
		mockMvc.perform(get("/api/v1/videos/list")
				.param("q", "q")
				.param("category", "cat")
				.param("maxResults", "3")
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].videoId").value("vid2"))
			.andExpect(jsonPath("$.data[0].bookmarked").value(true));
	}

	@Test
	@DisplayName("GET /api/v1/videos/{id}/analysis - SSE INIT 이벤트")
	void testVideoAnalysisInitEvent() throws Exception {
		// Given
		willDoNothing().given(videoService)
			.analyzeWithSseAsync(anyLong(), anyString(), any());

		// When & Then
		mockMvc.perform(get("/api/v1/videos/XYZ/analysis")
				.accept(MediaType.TEXT_EVENT_STREAM)
			)
			.andExpect(status().isOk())
			.andExpect(header().string(
				"Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE
			))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("event:INIT")))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("data:")));
	}
}
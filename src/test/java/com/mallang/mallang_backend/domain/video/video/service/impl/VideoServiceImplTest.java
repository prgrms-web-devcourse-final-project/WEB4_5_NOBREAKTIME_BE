package com.mallang.mallang_backend.domain.video.video.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

class VideoServiceImplTest {

	@Mock private MemberRepository memberRepository;
	@Mock private YoutubeService youtubeService;

	@Spy @InjectMocks private VideoServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("유효한 회원 언어로 검색 로직 호출")
	@Test
	void getVideosForMember_validLanguage_invokesGetVideosByLanguage() {
		// given
		Long memberId = 1L;
		Member member = Member.builder()
			.email("user@example.com")
			.password("pass")
			.nickname("nick")
			.profileImageUrl(null)
			.loginPlatform(LoginPlatform.NONE)
			.language(Language.ENGLISH)
			.build();
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		List<VideoResponse> mockList = List.of(new VideoResponse());
		doReturn(mockList).when(service).getVideosByLanguage("q", "cat", "en", 5L);

		// when
		List<VideoResponse> result = service.getVideosForMember("q", "cat", 5L, memberId);

		// then
		assertEquals(mockList, result);
		verify(service).getVideosByLanguage("q", "cat", "en", 5L);
	}

	@DisplayName("언어 설정 없을 시 예외 발생")
	@Test
	void getVideosForMember_noneLanguage_throwsLanguageError() {
		// given
		Long memberId = 2L;
		Member member = Member.builder()
			.email("none@example.com")
			.password("pass")
			.nickname("nick")
			.profileImageUrl(null)
			.loginPlatform(LoginPlatform.NONE)
			.language(Language.NONE)
			.build();
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

		// when & then
		ServiceException ex = assertThrows(ServiceException.class,
			() -> service.getVideosForMember(null, null, 10L, memberId)
		);
		assertEquals(ErrorCode.LANGUAGE_NOT_CONFIGURED, ex.getErrorCode());
	}

	@DisplayName("단건 상세조회 시 언어 매핑 정상")
	@Test
	void fetchDetail_successfulMapping() throws IOException {
		// given
		String vid = "vid1";
		Video ytVideo = new Video();
		VideoSnippet snippet = new VideoSnippet();
		snippet.setDefaultAudioLanguage("ja");
		snippet.setTitle("title");
		snippet.setDescription("desc");
		snippet.setChannelTitle("chan");
		Thumbnail thumb = new Thumbnail();
		thumb.setUrl("url");
		ThumbnailDetails thumbs = new ThumbnailDetails();
		thumbs.setMedium(thumb);
		snippet.setThumbnails(thumbs);
		ytVideo.setId(vid);
		ytVideo.setSnippet(snippet);

		when(youtubeService.fetchVideosByIds(List.of(vid)))
			.thenReturn(List.of(ytVideo));

		// when
		var resp = service.fetchDetail(vid);

		// then
		assertEquals(vid, resp.getVideoId());
		assertEquals(Language.JAPANESE, resp.getLanguage());
	}

	@DisplayName("단건 상세조회 IOException 시 오류 발생")
	@Test
	void fetchDetail_ioException_throwsDetailError() throws IOException {
		// given
		when(youtubeService.fetchVideosByIds(anyList()))
			.thenThrow(new IOException());

		// when & then
		ServiceException ex = assertThrows(ServiceException.class,
			() -> service.fetchDetail("x")
		);
		assertEquals(ErrorCode.VIDEO_DETAIL_FETCH_FAILED, ex.getErrorCode());
	}
}
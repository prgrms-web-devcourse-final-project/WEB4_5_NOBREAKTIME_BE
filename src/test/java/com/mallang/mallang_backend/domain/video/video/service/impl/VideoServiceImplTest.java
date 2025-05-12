package com.mallang.mallang_backend.domain.video.video.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.mallang.mallang_backend.domain.bookmark.repository.BookmarkRepository;
import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.stt.converter.Transcript;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptParser;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.event.KeywordSavedEvent;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.KeywordInfo;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.clova.ClovaSpeechClient;
import com.mallang.mallang_backend.global.util.clova.NestRequestEntity;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;
import com.mallang.mallang_backend.global.util.youtube.YoutubeAudioExtractor;


class VideoServiceImplTest {
	@Mock
	private MemberRepository memberRepository;

	@Spy @InjectMocks
	private VideoServiceImpl videoService;

	@Mock
	private VideoRepository videoRepository;

	@Mock
	private SubtitleRepository subtitleRepository;

	@Mock
	private KeywordRepository keywordRepository;

	@Mock
	private YoutubeAudioExtractor youtubeAudioExtractor;

	@Mock
	private ClovaSpeechClient clovaSpeechClient;

	@Mock
	private GptService gptService;

	@Mock
	private ApplicationEventPublisher publisher;

	@Mock
	private YoutubeService youtubeService;

	@Mock
	private TranscriptParser transcriptParser;

	@Mock
	private RedisDistributedLock redisDistributedLock;

	@Mock
	private BookmarkRepository bookmarkRepository;

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
		when(bookmarkRepository.findAllWithVideoByMemberId(memberId)).thenReturn(List.of());

		List<VideoResponse> mockList = List.of(new VideoResponse());
		doReturn(mockList)
				.when(videoService)
				.getVideosByLanguage(eq("q"), eq("cat"), eq("en"), eq(5L), anySet());

		// when
		List<VideoResponse> result = videoService.getVideosForMember("q", "cat", 5L, memberId);

		// then
		assertEquals(mockList, result);
		verify(videoService).getVideosByLanguage(eq("q"), eq("cat"), eq("en"), eq(5L), anySet());
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
			() -> videoService.getVideosForMember(null, null, 10L, memberId)
		);
		assertEquals(ErrorCode.LANGUAGE_NOT_CONFIGURED, ex.getErrorCode());
	}

	@Test
	@DisplayName("유튜브 ID로 영상을 분석할 수 있다")
	void analyzeVideo_newVideo() throws IOException, InterruptedException {
		// given
		Long memberId = 1L;
		String videoId = "test_video_id";
		String audioFile = "test_audio.mp3";

		Videos videoEntity = Videos.builder()
			.videoTitle("Test Video")
			.thumbnailImageUrl("thumbnail_url")
			.channelTitle("Test Channel")
			.language(Language.ENGLISH)
			.build();

		when(videoRepository.existsById(videoId)).thenReturn(false);
		when(videoRepository.save(any(Videos.class))).thenReturn(videoEntity);

		// Mock Thumbnail
		Thumbnail thumbnail = new Thumbnail();
		thumbnail.setUrl("http://example.com/thumb.jpg");

		ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
		thumbnailDetails.setMedium(thumbnail);

		// Mock Snippet
		VideoSnippet snippet = new VideoSnippet()
			.setTitle("테스트 영상")
			.setDescription("테스트 설명")
			.setChannelTitle("테스트 채널")
			.setDefaultAudioLanguage("en")
			.setThumbnails(thumbnailDetails);

		// Mock Video
		Video video = new Video()
			.setId(videoId)
			.setSnippet(snippet);

		// youtubeService Mock 설정
		when(youtubeService.fetchVideosByIds(List.of(videoId)))
			.thenReturn(List.of(video));

		when(youtubeAudioExtractor.extractAudio(anyString()))
			.thenReturn(audioFile);

		when(clovaSpeechClient.upload(any(File.class), any(NestRequestEntity.class)))
			.thenReturn("{\"segments\":[{\"start\":\"00:00:01\",\"end\":\"00:00:03\",\"text\":\"Hello world\"}]}");

		when(transcriptParser.parseTranscriptJson(anyString())).thenReturn(mock(Transcript.class));

		when(gptService.analyzeScript(anyList()))
			.thenReturn(List.of(
				new GptSubtitleResponse(
					1L,
					"00:00:01",
					"00:00:03",
					"Speaker 1",
					"Hello world",
					"안녕하세요 세상",
					List.of(new KeywordInfo("Hello", "인사", 1))
				)
			));
		when(redisDistributedLock.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);

		// when
		SseEmitter emitter = new SseEmitter(0L);
		AnalyzeVideoResponse response = videoService.analyzeVideo(memberId, videoId, emitter);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getSubtitleResults()).hasSize(1);

		// Verify Video
		verify(videoRepository, times(1)).save(any(Videos.class));

		// Verify Subtitle and Keyword save
		ArgumentCaptor<List<Subtitle>> subtitleCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<Keyword>> keywordCaptor = ArgumentCaptor.forClass(List.class);
		verify(subtitleRepository, times(1)).saveAll(subtitleCaptor.capture());
		verify(keywordRepository, times(1)).saveAll(keywordCaptor.capture());

		List<Subtitle> savedSubtitles = subtitleCaptor.getValue();
		List<Keyword> savedKeywords = keywordCaptor.getValue();

		assertThat(savedSubtitles).hasSize(1);
		Subtitle savedSubtitle = savedSubtitles.get(0);
		assertThat(savedSubtitle.getStartTime()).isEqualTo("00:00:01");
		assertThat(savedSubtitle.getEndTime()).isEqualTo("00:00:03");
		assertThat(savedSubtitle.getOriginalSentence()).isEqualTo("Hello world");
		assertThat(savedSubtitle.getTranslatedSentence()).isEqualTo("안녕하세요 세상");
		assertThat(savedSubtitle.getSpeaker()).isEqualTo("Speaker 1");

		assertThat(savedKeywords).hasSize(1);
		Keyword savedKeyword = savedKeywords.get(0);
		assertThat(savedKeyword.getWord()).isEqualTo("Hello");
		assertThat(savedKeyword.getMeaning()).isEqualTo("인사");
		assertThat(savedKeyword.getDifficulty().getValue()).isEqualTo(1);

		// Verify Event Publishing
		ArgumentCaptor<KeywordSavedEvent> eventCaptor = ArgumentCaptor.forClass(KeywordSavedEvent.class);
		verify(publisher, times(1)).publishEvent(eventCaptor.capture());
		KeywordSavedEvent event = eventCaptor.getValue();
		assertThat(event.getKeyword().getWord()).isEqualTo("Hello");
	}
}
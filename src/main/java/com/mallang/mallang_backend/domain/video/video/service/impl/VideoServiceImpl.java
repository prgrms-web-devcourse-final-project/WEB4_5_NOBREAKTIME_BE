package com.mallang.mallang_backend.domain.video.video.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.stt.converter.Transcript;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptParser;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.SearchContext;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetail;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.event.KeywordSavedEvent;
import com.mallang.mallang_backend.domain.video.video.event.VideoAnalyzedEvent;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.domain.videohistory.event.VideoViewedEvent;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.KeywordInfo;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.clova.ClovaSpeechClient;
import com.mallang.mallang_backend.global.util.clova.NestRequestEntity;
import com.mallang.mallang_backend.global.util.youtube.YoutubeAudioExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

	private final VideoRepository videoRepository;
	private final VideoSearchProperties youtubeSearchProperties;
	private final YoutubeService youtubeService;
	private final YoutubeAudioExtractor youtubeAudioExtractor;
	private final GptService gptService;
	private final SubtitleRepository subtitleRepository;
	private final TranscriptParser transcriptParser;
	private final MemberRepository memberRepository;
	private final ClovaSpeechClient clovaSpeechClient;
	private final KeywordRepository keywordRepository;
	private final ApplicationEventPublisher publisher;

	// 회원 기준 영상 검색 메서드
	@Override
	public List<VideoResponse> getVideosForMember(
		String q,
		String category,
		long maxResults,
		Long memberId
	) {
		// 회원 조회해서 언어 정보 가져오기
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		Language lang = member.getLanguage();
		if (lang == Language.NONE) {
			throw new ServiceException(ErrorCode.LANGUAGE_NOT_CONFIGURED);
		}

		// ISO 코드 추출
		String language = lang.toCode();

		// 검색 컨텍스트 준비
		return getVideosByLanguage(q, category, language, maxResults);
	}

	@Override
	public List<VideoResponse> getVideosByLanguage(
		String q,
		String category,
		String language,
		long maxResults
	) {
		// 검색 컨텍스트 준비
		SearchContext ctx = buildSearchContext(q, category, language);
		log.info("context: {}", ctx);

		// ID 목록 조회
		List<String> videoIds = fetchVideoIds(ctx, maxResults);
		if (videoIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 상세 비디오(YouTube 모델) 조회
		List<Video> videos = fetchVideoDetails(videoIds);

		// 필터링, 매핑, 셔플 (VideoUtils 이용)
		List<VideoResponse> responses = videos.stream()
			.filter(VideoUtils::isCreativeCommons)
			.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
			.filter(VideoUtils::isDurationLessThanOrEqualTo20Minutes)
			.map(VideoUtils::toVideoResponse)
			.toList();

		return VideoUtils.shuffleIfDefault(responses, ctx.isDefaultSearch());
	}

	/**
	 * 지정된 YouTube 비디오 ID의 상세 정보를 조회해 DTO로 반환
	 *
	 * @param videoId YouTube 비디오 ID
	 * @return 조회된 비디오 정보 DTO
	 */
	private VideoDetail fetchDetail(String videoId) {
		List<Video> ytVideos = youtubeService.fetchVideosByIds(List.of(videoId));
		var ytVideo = ytVideos.stream()
			.findFirst()
			.orElseThrow(() -> new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED));

		// 언어 정보 파싱
		Language lang = Language.fromCode(ytVideo.getSnippet().getDefaultAudioLanguage());

		// 응답 DTO 생성
		return new VideoDetail(
			ytVideo.getId(),
			ytVideo.getSnippet().getTitle(),
			ytVideo.getSnippet().getDescription(),
			ytVideo.getSnippet().getThumbnails().getMedium().getUrl(),
			ytVideo.getSnippet().getChannelTitle(),
			lang
		);
	}

	/**
	 * VideoDetailResponse를 기반으로 Videos 엔티티를 INSERT 혹은 UPDATE
	 *
	 * @param dto
	 */
	private Videos upsertVideoEntity(VideoDetail dto) {
		String id = dto.getVideoId();

		// DB에 해당 ID가 이미 있으면 필드 업데이트
		if (videoRepository.existsById(id)) {
			Videos existing = videoRepository.getReferenceById(id);
			existing.updateTitleAndThumbnail(
				dto.getTitle(),
				dto.getThumbnailUrl(),
				dto.getChannelTitle(),
				dto.getLanguage()
			);
			return existing;
		} else {
			// 없으면 새로 저장
			Videos entity = VideoDetail.toEntity(dto);
			return videoRepository.save(entity);
		}
	}

	/**
	 * 검색 컨텍스트 빌더 (검색 요청에 필요한 모든 파라미터를 한 번에 묶어주는 역할)
	 */
	private SearchContext buildSearchContext(String q, String category, String language) {
		String langKey = Optional.ofNullable(language)
			.filter(StringUtils::hasText)
			.map(String::toLowerCase)
			.orElse("en");

		var defaults = youtubeSearchProperties.getDefaults()
			.getOrDefault(langKey, youtubeSearchProperties.getDefaults().get("en"));
		String region = defaults.getRegion();
		String query = StringUtils.hasText(q) ? q : defaults.getQuery();
		boolean isDefault = !StringUtils.hasText(q) && !StringUtils.hasText(category);

		return new SearchContext(query, region, langKey, category, isDefault);
	}

	/**
	 * YouTube ID 목록으로 Video 모델 조회
	 */
	private List<Video> fetchVideoDetails(List<String> ids) {
		return youtubeService.fetchVideosByIds(ids);
	}

	/**
	 * ID 목록 조회를 위한 YouTube search 호출
	 */
	private List<String> fetchVideoIds(SearchContext ctx, long maxResults) {
		try {
			return youtubeService.searchVideoIds(
				ctx.getQuery(),
				ctx.getRegion(),
				ctx.getLangKey(),
				ctx.getCategory(),
				maxResults
			);
		} catch (IOException e) {
			throw new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED);
		}
	}

	@Transactional
	@Override
	public AnalyzeVideoResponse analyzeVideo(Long memberId, String videoId) throws IOException, InterruptedException {
		// 영상 정보 저장
		VideoDetail dto = fetchDetail(videoId);
		Videos video = upsertVideoEntity(dto);

		// 비디오 히스토리 저장 이벤트
		publisher.publishEvent(new VideoViewedEvent(memberId, videoId));

		// 기존 분석 결과 확인
		List<Subtitle> existing = subtitleRepository.findAllByVideosFetchKeywords(video);
		if (!existing.isEmpty()) {
			List<GptSubtitleResponse> subtitleResponses = GptSubtitleResponse.from(existing);
			return AnalyzeVideoResponse.from(subtitleResponses);
		}

		// 2. 음성 추출
		String fileName = youtubeAudioExtractor.extractAudio(YOUTUBE_VIDEO_BASE_URL + videoId);

		// 3. STT
		NestRequestEntity requestEntity = new NestRequestEntity(video.getLanguage());
		final String result =
			clovaSpeechClient.upload(new File(UPLOADS_DIR + fileName), requestEntity);

		// STT 결과 → 세그먼트 파싱
		Transcript transcript = transcriptParser.parseTranscriptJson(result);
		List<TranscriptSegment> segments = transcript.getSegments();

		// GPT 분석
		List<GptSubtitleResponse> gptResult = gptService.analyzeScript(segments);

		saveSubtitleAndKeyword(video, gptResult);

		// 비동기로 오디오 파일 삭제
		publisher.publishEvent(new VideoAnalyzedEvent(fileName));

		return AnalyzeVideoResponse.from(gptResult);
	}

	private void saveSubtitleAndKeyword(Videos video, List<GptSubtitleResponse> gptResult) {
		List<Subtitle> subtitleList = new ArrayList<>();
		List<Keyword> keywordList = new ArrayList<>();

		for (GptSubtitleResponse response : gptResult) {
			// Subtitle 엔티티 생성
			Subtitle subtitle = Subtitle.builder()
				.videos(video)
				.startTime(response.getStartTime())
				.endTime(response.getEndTime())
				.originalSentence(response.getOriginal())
				.translatedSentence(response.getTranscript())
				.speaker(response.getSpeaker())
				.build();

			subtitleList.add(subtitle);

			// Keyword 리스트 생성
			if (response.getKeywords() != null) {
				for (KeywordInfo keywordInfo : response.getKeywords()) {
					Keyword keyword = keywordInfo.toEntity(video, subtitle);
					keywordList.add(keyword);
				}
			}
		}

		// subtitle 먼저 저장 (ID를 키로 사용하는 keyword 저장을 위해)
		subtitleRepository.saveAll(subtitleList);

		// keyword 저장
		keywordRepository.saveAll(keywordList);

		// 비동기로 핵심단어들 gpt 사용하여 단어DB에 저장
		keywordList.forEach(k -> publisher.publishEvent(new KeywordSavedEvent(k)));
	}
}

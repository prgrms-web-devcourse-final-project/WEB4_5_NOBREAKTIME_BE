package com.mallang.mallang_backend.domain.video.video.service.impl;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.bookmark.repository.BookmarkRepository;
import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.keyword.repository.KeywordRepository;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.stt.converter.Transcript;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptParser;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.dto.AnalyzeVideoResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetail;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.event.KeywordSavedEvent;
import com.mallang.mallang_backend.domain.video.video.event.VideoAnalyzedEvent;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
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
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;
import com.mallang.mallang_backend.global.util.sse.SseEmitterManager;
import com.mallang.mallang_backend.global.util.youtube.YoutubeAudioExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.constants.AppConstants.UPLOADS_DIR;
import static com.mallang.mallang_backend.global.constants.AppConstants.YOUTUBE_VIDEO_BASE_URL;
import static com.mallang.mallang_backend.global.exception.ErrorCode.ANALYZE_VIDEO_CONCURRENCY_TIME_OUT;
import static com.mallang.mallang_backend.global.exception.ErrorCode.VIDEO_ANALYSIS_FAILED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

	private final VideoRepository videoRepository;
	private final YoutubeService youtubeService;
	private final YoutubeAudioExtractor youtubeAudioExtractor;
	private final GptService gptService;
	private final SubtitleRepository subtitleRepository;
	private final TranscriptParser transcriptParser;
	private final MemberRepository memberRepository;
	private final ClovaSpeechClient clovaSpeechClient;
	private final KeywordRepository keywordRepository;
	private final ApplicationEventPublisher publisher;
	private final RedisDistributedLock redisDistributedLock;
	private final AnalyzeVideoResultFetcher analyzeVideoResultFetcher;
	private final BookmarkRepository bookmarkRepository;
	private final VideoQueryService videoQueryService;
	private final SseEmitterManager sseEmitterManager;

	// 회원 기준 영상 검색 메서드
	@Override
	public List<VideoResponse> getVideosForMember(String q, String category, long maxResults, Long memberId) {
		// 회원 조회해서 언어 정보 가져오기
		Member member = memberRepository.findById(memberId).orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		Language lang = member.getLanguage();
		if (lang == Language.NONE) {
			throw new ServiceException(ErrorCode.LANGUAGE_NOT_CONFIGURED);
		}

		// 북마크된 videoId 목록 조회
		Set<String> bookmarkedIds = bookmarkRepository.findAllWithVideoByMemberId(memberId).stream().map(bookmark -> bookmark.getVideos().getId()).collect(Collectors.toSet());

		// ISO 코드 추출
		String language = lang.toCode();

		// 검색 컨텍스트 준비
		return getVideosByLanguage(q, category, language, maxResults, bookmarkedIds);
	}

	@Override
	public List<VideoResponse> getVideosByLanguage(String q, String category, String language, long maxResults, Set<String> bookmarkedIds) {
		// 캐시가 적용된 queryVideos() 호출
		List<VideoResponse> responses = videoQueryService.queryVideos(q, category, language, maxResults);

		// 북마크 여부 세팅
		responses.forEach(r -> r.setBookmarked(bookmarkedIds.contains(r.getVideoId())));

		return responses;
	}

	/**
	 * 지정된 YouTube 비디오 ID의 상세 정보를 조회해 DTO로 반환
	 *
	 * @param videoId YouTube 비디오 ID
	 * @return 조회된 비디오 정보 DTO
	 */
	private VideoDetail fetchDetail(String videoId) {
		List<Video> ytVideos = youtubeService.fetchVideosByIdsAsync(List.of(videoId)).join();
		var ytVideo = ytVideos.stream().findFirst().orElseThrow(() -> new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED));

		// 언어 정보 파싱
		Language lang = Language.fromCode(ytVideo.getSnippet().getDefaultAudioLanguage());

		// 응답 DTO 생성
		return new VideoDetail(ytVideo.getId(), ytVideo.getSnippet().getTitle(), ytVideo.getSnippet().getDescription(), ytVideo.getSnippet().getThumbnails().getMedium().getUrl(), ytVideo.getSnippet().getChannelTitle(), lang, ytVideo.getContentDetails().getDuration());
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
			existing.updateTitleAndThumbnail(dto.getTitle(), dto.getThumbnailUrl(), dto.getChannelTitle(), dto.getLanguage(), dto.getDuration());
			return existing;
		} else {
			// 없으면 새로 저장
			Videos entity = VideoDetail.toEntity(dto);
			return videoRepository.save(entity);
		}
	}

	@Async("analysisExecutor")
	@Transactional
	@Override
	public void analyzeWithSseAsync(Long memberId, String videoId, String emitterId) {
		try {
			AnalyzeVideoResponse result = analyzeVideo(memberId, videoId, emitterId);
			sseEmitterManager.sendTo(emitterId, "analysisComplete", result);
		} catch (Exception e) {
			// 분석 실패 이벤트 전송
			sseEmitterManager.sendTo(emitterId, "videoAnalysisFailed", "영상 분석에 실패했습니다.");
			log.warn("영상 분석 중 에러", e);
		} finally {
			sseEmitterManager.removeEmitter(emitterId);
		}
	}

	private AnalyzeVideoResponse analyzeVideo(Long memberId, String videoId, String emitterId) {
		Member member = memberRepository.findById(memberId).orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		long startTotal = System.nanoTime(); // 전체 시작 시간
		log.debug("[AnalyzeVideo] 시작 - videoId: {}", videoId);

		// 1. 비디오 히스토리 저장 (비동기 요청)
		long start = System.nanoTime();
		publisher.publishEvent(new VideoViewedEvent(memberId, videoId));
		log.debug("[AnalyzeVideo] 시청 히스토리 이벤트 발행 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

		// 2. 기존 분석 결과 확인
		List<Subtitle> existing = subtitleRepository.findAllByVideosFetchKeywords(videoId);
		if (!existing.isEmpty()) {
			List<GptSubtitleResponse> subtitleResponses = GptSubtitleResponse.from(existing);
			log.debug("[AnalyzeVideo] 기존 분석 결과 반환 ({} ms)", (System.nanoTime() - start) / 1_000_000);
			log.info("[AnalyzeVideo] 전체 완료 ({} ms)", (System.nanoTime() - startTotal) / 1_000_000);
			return AnalyzeVideoResponse.from(subtitleResponses);
		}

		// 락 획득 시도
		String lockKey = "lock:video:analysis:" + videoId;
		String lockValue = UUID.randomUUID().toString();
		long ttlMillis = Duration.ofMinutes(10).toMillis();

		boolean locked = redisDistributedLock.tryLock(lockKey, lockValue, ttlMillis);
		if (!locked) {
			sseEmitterManager.sendTo(emitterId, "lockChecking", "동일한 영상의 분석이 진행중입니다...");

			// 락이 사라졌는지 10분간 계속 확인
			boolean lockAvailable = redisDistributedLock.waitForUnlockThenFetch(lockKey, ttlMillis, 2000L);

			// 최대 재시도 시간까지 확인했으나 실패함
			if (!lockAvailable) {
				throw new ServiceException(ANALYZE_VIDEO_CONCURRENCY_TIME_OUT);
			}

			// 락이 사라졌으면 다른 작업으로 처리된 결과를 DB에서 찾아서 응답
			return analyzeVideoResultFetcher.fetchAnalyzedResultAfterWait(videoId);
		}

		String fileName = null;
		try {
			// **락 획득 알림**
			sseEmitterManager.sendTo(emitterId, "lockAcquired","Lock acquired, 곧 Audio 추출 시작합니다.");

			// 3. 영상 정보 저장
			start = System.nanoTime();
			VideoDetail dto = fetchDetail(videoId);
			Videos video = upsertVideoEntity(dto);
			log.debug("[AnalyzeVideo] 영상 정보 저장 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			// 4. 음성 추출
			start = System.nanoTime();
			fileName = youtubeAudioExtractor.extractAudio(YOUTUBE_VIDEO_BASE_URL + videoId);
			log.debug("[AnalyzeVideo] 오디오 추출 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			// **오디오 추출 완료 알림**
			sseEmitterManager.sendTo(emitterId, "audioExtracted","Audio 추출 완료, STT 분석 시작합니다.");

			// 5. STT 요청
			start = System.nanoTime();
			NestRequestEntity requestEntity = new NestRequestEntity(video.getLanguage());
			final String result = clovaSpeechClient.upload(new File(UPLOADS_DIR + fileName), requestEntity);
			log.debug("[AnalyzeVideo] STT 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			// **STT 완료 알림**
			sseEmitterManager.sendTo(emitterId, "sttCompleted","STT 완료, GPT 분석 시작합니다.");

			// 6. STT 결과 파싱
			start = System.nanoTime();
			Transcript transcript = transcriptParser.parseTranscriptJson(result);
			List<TranscriptSegment> segments = transcript.getSegments();
			log.debug("[AnalyzeVideo] STT 결과 파싱 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			// 7. GPT 분석
			start = System.nanoTime();
			List<GptSubtitleResponse> gptResult = gptService.analyzeScript(segments, member.getLanguage());
			// if (isInvalidGptResult(gptResult)) {
			// 	throw new ServiceException(INVALID_GPT_RESPONSE);
			// }
			log.debug("[AnalyzeVideo] GPT 분석 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			// 8. 저장
			start = System.nanoTime();
			saveSubtitleAndKeyword(video, gptResult, member.getLanguage());
			log.debug("[AnalyzeVideo] 결과 저장 완료 ({} ms)", (System.nanoTime() - start) / 1_000_000);

			return AnalyzeVideoResponse.from(gptResult);
		} catch (IOException | InterruptedException | ServiceException e) {
			sseEmitterManager.sendTo(emitterId, "videoAnalysisFailed", "영상 분석에 실패했습니다.");
			log.warn("영상 분석 실패", e);
			throw new ServiceException(VIDEO_ANALYSIS_FAILED);
		} finally {
			// 락 해제
			redisDistributedLock.unlock(lockKey, lockValue);
			// 9. 파일 삭제 이벤트
			if (fileName != null) {
				publisher.publishEvent(new VideoAnalyzedEvent(fileName));
				log.debug("[AnalyzeVideo] 오디오 삭제 이벤트 발생");
			}
			log.info("[AnalyzeVideo] 전체 완료 ({} ms)", (System.nanoTime() - startTotal) / 1_000_000);
		}
	}

	private boolean isInvalidGptResult(List<GptSubtitleResponse> gptResult) {
		return gptResult.stream()
				.anyMatch(r -> r.getKeywords().isEmpty());
	}

	private void saveSubtitleAndKeyword(Videos video, List<GptSubtitleResponse> gptResult, Language language) {
		List<Subtitle> subtitleList = new ArrayList<>();
		List<Keyword> keywordList = new ArrayList<>();

		for (GptSubtitleResponse response : gptResult) {
			// Subtitle 엔티티 생성
			Subtitle subtitle = Subtitle.builder().videos(video).startTime(response.getStartTime()).endTime(response.getEndTime()).originalSentence(response.getOriginal()).translatedSentence(response.getTranscript()).speaker(response.getSpeaker()).build();

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
		List<Subtitle> savedSubtitles = subtitleRepository.saveAll(subtitleList);

		// 저장된 엔티티의 PK(id)를 순서대로 DTO에 주입
		for (int i = 0; i < savedSubtitles.size(); i++) {
			gptResult.get(i).setSubtitleId(savedSubtitles.get(i).getId());
		}

		// keyword 저장
		keywordRepository.saveAll(keywordList);

		// 비동기로 핵심단어들 gpt 사용하여 단어DB에 저장
		keywordList.forEach(k -> publisher.publishEvent(new KeywordSavedEvent(k, language)));
	}

	@Override
	@Transactional
	public Videos saveVideoIfAbsent(String videoId) {
		return videoRepository.findById(videoId).orElseGet(() -> {
			VideoDetail dto = fetchDetail(videoId);
			return upsertVideoEntity(dto);
		});
	}
}

package com.mallang.mallang_backend.domain.video.video.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetailResponse;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
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
	 * @param videoId YouTube 비디오 ID
	 * @return 조회된 비디오 정보 DTO
	 */
	@Override
	public VideoDetailResponse fetchDetail(String videoId) {
		try {
			List<Video> ytVideos = youtubeService.fetchVideosByIds(List.of(videoId));
			var ytVideo = ytVideos.stream()
				.findFirst()
				.orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

			// 언어 정보 파싱
			Language lang = Language.fromCode(ytVideo.getSnippet().getDefaultAudioLanguage());

			// 응답 DTO 생성
			return new VideoDetailResponse(
				ytVideo.getId(),
				ytVideo.getSnippet().getTitle(),
				ytVideo.getSnippet().getDescription(),
				ytVideo.getSnippet().getThumbnails().getMedium().getUrl(),
				ytVideo.getSnippet().getChannelTitle(),
				lang
			);
		} catch (IOException e) {
			throw new ServiceException(ErrorCode.VIDEO_DETAIL_FETCH_FAILED);
		}
	}

	/**
	 * 비디오 상세 정보를 조회(fetchDetail 호출)하고, 해당 정보를 DB에 저장 또는 업데이트
	 *
	 * @param videoId YouTube 비디오 ID
	 * @return 조회 및 저장 완료된 비디오 정보 DTO
	 */
	/**
	 * YouTube에서 상세정보 조회(fetch) 후
	 * DB에 upsert하고 DTO 반환
	 */
	@Override
	@Transactional
	public VideoDetailResponse getVideoDetail(String videoId) {
		try {
			// YouTube API 호출
			List<Video> ytList = youtubeService.fetchVideosByIds(List.of(videoId));
			Video yt = ytList.stream()
				.findFirst()
				.orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

			// DTO 생성
			Language lang = Language.fromCode(yt.getSnippet().getDefaultAudioLanguage());
			VideoDetailResponse dto = new VideoDetailResponse(
				yt.getId(),
				yt.getSnippet().getTitle(),
				yt.getSnippet().getDescription(),
				yt.getSnippet().getThumbnails().getMedium().getUrl(),
				yt.getSnippet().getChannelTitle(),
				lang
			);

			// DB upsert
			if (videoRepository.existsById(dto.getVideoId())) {
				Videos existing = videoRepository.getReferenceById(dto.getVideoId());
				existing.updateTitleAndThumbnail(
					dto.getTitle(),
					dto.getThumbnailUrl(),
					dto.getChannelTitle(),
					dto.getLanguage()
				);
			} else {
				videoRepository.save(VideoDetailResponse.toEntity(dto));
			}

			return dto;
		} catch (IOException e) {
			throw new ServiceException(VIDEO_DETAIL_FETCH_FAILED);
		}
	}

	/**
	 * VideoDetailResponse를 기반으로 Videos 엔티티를 INSERT 혹은 UPDATE
	 * @param dto
	 */
	@Transactional
	protected void upsertVideoEntity(VideoDetailResponse dto) {
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
		} else {
			// 없으면 새로 저장
			Videos entity = VideoDetailResponse.toEntity(dto);
			videoRepository.save(entity);
		}
	}

	/** 검색 컨텍스트 빌더 (검색 요청에 필요한 모든 파라미터를 한 번에 묶어주는 역할) */
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

	/** YouTube ID 목록으로 Video 모델 조회 */
	private List<Video> fetchVideoDetails(List<String> ids) {
		try {
			return youtubeService.fetchVideosByIds(ids);
		} catch (IOException e) {
			throw new ServiceException(ErrorCode.VIDEO_DETAIL_FETCH_FAILED);
		}
	}

	/** ID 목록 조회를 위한 YouTube search 호출 */
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
			throw new ServiceException(VIDEO_ID_SEARCH_FAILED);
		}
	}

	@Override
	public String analyzeVideo(String videoUrl) throws IOException, InterruptedException {
		// 오디오 추출
		String fileName = youtubeAudioExtractor.extractAudio(videoUrl);
		// 추가 처리 TODO
		return fileName;
	}

	@Override
	public byte[] getAudioFile(String fileName) throws IOException {
		Path path = Paths.get(UPLOADS_DIR, fileName);
		return java.nio.file.Files.readAllBytes(path);
	}
}

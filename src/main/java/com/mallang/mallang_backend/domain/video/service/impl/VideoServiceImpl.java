package com.mallang.mallang_backend.domain.video.service.impl;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.service.VideoService;
import com.mallang.mallang_backend.domain.video.util.VideoUtils;
import com.mallang.mallang_backend.domain.video.youtube.config.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.youtube.service.YoutubeService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.YoutubeAudioExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

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

		// 상세 비디오 조회
		List<Video> videos = fetchVideoDetails(videoIds);

		// 필터링, 매핑, 셔플
		List<VideoResponse> responses = videos.stream()
			.filter(VideoUtils::isCreativeCommons)
			.filter(v -> VideoUtils.matchesLanguage(v, ctx.getLangKey()))
			.filter(VideoUtils::isDurationLessThanOrEqualTo20Minutes)
			.map(VideoUtils::toVideoResponse)
			.toList();

		return VideoUtils.shuffleIfDefault(responses, ctx.isDefaultSearch());
	}

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

	private List<Video> fetchVideoDetails(List<String> ids) {
		try {
			return youtubeService.fetchVideosByIds(ids);
		} catch (IOException e) {
			throw new ServiceException(ErrorCode.VIDEO_DETAIL_FETCH_FAILED);
		}
	}

	@Override
	public String analyzeVideo(String videoUrl) throws IOException, InterruptedException {
		String fileName = youtubeAudioExtractor.extractAudio(videoUrl);

		// TODO: fileName 으로 리소스 링크 만들어서 Clova Speech 한테 넘겨주고 응답으로 스크립트를 받는다.

		// TODO: OpenAI로 핵심 단어 추출하고, 번역한다.

		return fileName;
	}

	@Override
	public byte[] getAudioFile(String fileName) throws IOException {
		Path path = Paths.get(UPLOADS_DIR, fileName);
		return Files.readAllBytes(path);
	}
}

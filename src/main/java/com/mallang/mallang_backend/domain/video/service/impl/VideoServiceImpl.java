package com.mallang.mallang_backend.domain.video.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mallang.mallang_backend.domain.video.VideoSearchProperties;
import com.mallang.mallang_backend.domain.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    // yml 매핑한 설정 클래스 주입
    private final VideoSearchProperties youtubeSearchProperties;

    // 유튜브 API 키
    @Value("${youtube.api.key}")
    private String apiKey;

    @Override
    public List<VideoResponse> getVideosByLanguage(String language, long maxResults) {
        List<VideoResponse> results = new ArrayList<>();

        try {
            String langKey = language.toLowerCase();

            // 언어에 맞는 기본 검색어와 지역코드를 가져온다
            VideoSearchProperties.SearchDefault searchDefault = youtubeSearchProperties.getDefaults().get(langKey);

            if (searchDefault == null) {
                // 언어 매칭이 안되면 기본 en 사용
                searchDefault = youtubeSearchProperties.getDefaults().get("en");
            }

            String effectiveQuery = searchDefault.getQuery();
            String effectiveRegionCode = searchDefault.getRegion();
            String effectiveLanguageCode = langKey;

            // 유튜브 API 클라이언트 생성
            YouTube youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    request -> {
                    }
            ).setApplicationName("youtube-search-app")
                    .build();

            // 검색 요청 세팅
            YouTube.Search.List searchRequest = youtubeService.search()
                    .list(List.of("id", "snippet"));

            searchRequest.setQ(effectiveQuery);
            searchRequest.setType(List.of("video"));
            searchRequest.setVideoLicense("creativeCommon");
            searchRequest.setRelevanceLanguage(effectiveLanguageCode);
            searchRequest.setRegionCode(effectiveRegionCode);
            searchRequest.setMaxResults(maxResults);
            searchRequest.setVideoDuration("any");
            searchRequest.setKey(apiKey);

            // 검색 실행
            SearchListResponse searchResponse = searchRequest.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            if (searchResults != null && !searchResults.isEmpty()) {
                // videoId 리스트 추출
                List<String> videoIds = searchResults.stream()
                        .map(searchResult -> searchResult.getId().getVideoId())
                        .collect(Collectors.toList());

                // YouTube Videos API 호출 (duration 정보 조회)
                YouTube.Videos.List videosRequest = youtubeService.videos()
                        .list(List.of("id", "snippet", "contentDetails"));

                videosRequest.setId(videoIds);
                videosRequest.setKey(apiKey);

                VideoListResponse videosResponse = videosRequest.execute();
                List<Video> videoList = videosResponse.getItems();

                for (Video video : videoList) {
                    String durationStr = video.getContentDetails().getDuration();
                    Duration duration = parseDuration(durationStr); // PT19M32S 같은 걸 파싱

                    if (duration != null && duration.toMinutes() <= 20) {
                        // 20분 이하만 리스트에 추가
                        results.add(new VideoResponse(
                                video.getId(),
                                video.getSnippet().getTitle(),
                                video.getSnippet().getDescription(),
                                video.getSnippet().getThumbnails().getMedium().getUrl()
                        ));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    // SO 8601 포맷(PT19M32S)을 java.time.Duration 객체로 변환
    private Duration parseDuration(String isoDuration) {
        try {
            return Duration.parse(isoDuration);
        } catch (Exception e) {
            return null;
        }
    }
}
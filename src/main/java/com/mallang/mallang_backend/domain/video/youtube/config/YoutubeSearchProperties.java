package com.mallang.mallang_backend.domain.video.youtube.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "youtube.search") // yml 파일에서 "youtube.search" 하위 값을 읽어와서 매핑
public class YoutubeSearchProperties {
    private Map<String, SearchDefault> defaults;

    // 언어 코드(en, ja, zh) 별로 SearchDefault 객체를 매핑
    @Data
    public static class SearchDefault {
        private String query;
        private String region;
        private String videoDuration;
    }
}
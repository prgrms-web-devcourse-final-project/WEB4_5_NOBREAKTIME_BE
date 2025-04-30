package com.mallang.mallang_backend.domain.videohistory.mapper;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import org.springframework.stereotype.Component;

@Component
public class VideoHistoryMapper {

    /**
     * VideoHistory 엔티티를 DTO로 변환
     * 연관된 Video 엔티티에서 직접 데이터 추출
     */
    public VideoHistoryResponse toDto(VideoHistory videoHistory) {
        var video = videoHistory.getVideos();
        return new VideoHistoryResponse(
            video.getId(),
            video.getVideoTitle(),
            video.getThumbnailImageUrl(),
            videoHistory.getCreatedAt()
        );
    }
}

package com.mallang.mallang_backend.domain.videohistory.mapper;

import com.mallang.mallang_backend.domain.video.video.entity.Video;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoHistoryMapper {

    private final VideoRepository videoRepository;

    public VideoHistoryResponse toDto(VideoHistory videoHistory) {
        Long videoId = videoHistory.getId().getVideoId();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found with id: " + videoId));

        return new VideoHistoryResponse(
                videoId,
                video.getVideoTitle(),
                video.getThumbnailImageUrl(),
                videoHistory.getCreatedAt()
        );
    }
}
package com.mallang.mallang_backend.domain.video.video.factory;

import org.springframework.stereotype.Component;
import com.mallang.mallang_backend.domain.video.video.dto.VideoDetailResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;

@Component
public class VideosFactory {

	/**
	 * YouTube API에서 가져온 Video 객체를 Videos 엔티티로 변환
	 */
	public Videos fromDto(VideoDetailResponse dto) {
		return Videos.builder()
			.id(dto.getVideoId())
			.videoTitle(dto.getTitle())
			.thumbnailImageUrl(dto.getThumbnailUrl())
			.channelTitle(dto.getChannelTitle())
			.language(dto.getLanguage())
			.build();
	}
}


package com.mallang.mallang_backend.domain.video.video.dto;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.common.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailResponse {
	private String videoId;
	private String title;
	private String description;
	private String thumbnailUrl;
	private String channelTitle;
	private Language language;

	public static Videos toEntity(VideoDetailResponse dto) {
		return Videos.builder()
			.id(dto.getVideoId())
			.videoTitle(dto.getTitle())
			.thumbnailImageUrl(dto.getThumbnailUrl())
			.channelTitle(dto.getChannelTitle())
			.language(dto.getLanguage())
			.build();
	}
}
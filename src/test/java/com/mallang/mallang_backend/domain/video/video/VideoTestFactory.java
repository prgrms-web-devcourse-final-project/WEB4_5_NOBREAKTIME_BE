package com.mallang.mallang_backend.domain.video.video;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.common.Language;

public class VideoTestFactory {
	public static Videos createDefault() {
		return Videos.builder()
			.id("testId")
			.videoTitle("테스트 비디오")
			.thumbnailImageUrl("http://example.com/thumbnail.jpg")
			.channelTitle("테스트 채널")
			.language(Language.ENGLISH)
			.build();
	}

	public static Videos create(String id, String title, Language lang) {
		return Videos.builder()
			.id(id)
			.videoTitle(title)
			.thumbnailImageUrl("http://example.com/" + id + ".jpg")
			.channelTitle("채널-" + id)
			.language(lang)
			.build();
	}
}

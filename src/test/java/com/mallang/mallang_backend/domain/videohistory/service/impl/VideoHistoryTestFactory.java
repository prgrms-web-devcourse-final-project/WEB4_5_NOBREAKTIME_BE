package com.mallang.mallang_backend.domain.videohistory.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.global.common.Language;

public class VideoHistoryTestFactory {

	public static Member createMember(Long id) {
		Member m = Member.builder()
			.email(UUID.randomUUID() + "@test.com")
			.language(com.mallang.mallang_backend.global.common.Language.ENGLISH)
			.loginPlatform(com.mallang.mallang_backend.domain.member.entity.LoginPlatform.KAKAO)
			.nickname("nick-"+UUID.randomUUID())
			.platformId("plat-"+UUID.randomUUID())
			.profileImageUrl("")
			.build();
		ReflectionTestUtils.setField(m, "id", id);
		return m;
	}

	public static Videos createVideos(String id) {
		Videos v = Videos.builder()
			.id(id)
			.videoTitle("제목")
			.thumbnailImageUrl("thumb.png")
			.channelTitle("채널")
			.language(Language.ENGLISH)
			.build();
		return v;
	}

	public static VideoHistory createVideoHistory(
		Long id,
		Member member,
		Videos videos,
		LocalDateTime lastViewedAt
	) {
		VideoHistory h = VideoHistory.builder()
			.member(member)
			.videos(videos)
			.build();
		ReflectionTestUtils.setField(h, "id", id);
		ReflectionTestUtils.setField(h, "lastViewedAt", lastViewedAt);
		return h;
	}
}

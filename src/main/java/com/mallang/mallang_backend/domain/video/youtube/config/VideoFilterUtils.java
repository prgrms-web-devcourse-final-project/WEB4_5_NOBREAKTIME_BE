package com.mallang.mallang_backend.domain.video.youtube.config;

import com.google.api.services.youtube.model.Video;
import com.mallang.mallang_backend.domain.video.youtube.mapper.DurationMapper;
import java.time.Duration;

public class VideoFilterUtils {

	// 20분 이하 영상인지 체크
	public static boolean isDurationLessThanOrEqualTo20Minutes(Video video) {
		String durationStr = video.getContentDetails().getDuration();
		Duration duration = DurationMapper.parseDuration(durationStr);

		return duration != null && duration.toMinutes() <= 20;
	}
}

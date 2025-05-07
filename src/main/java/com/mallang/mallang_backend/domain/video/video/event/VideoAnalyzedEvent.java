package com.mallang.mallang_backend.domain.video.video.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoAnalyzedEvent {
	private String fileName;
}

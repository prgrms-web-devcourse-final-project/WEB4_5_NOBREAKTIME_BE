package com.mallang.mallang_backend.domain.video.youtube.client;

import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

@Component
public class YoutubeClient {

	// Youtube API 클라이언트 생성
	public static YouTube getClient() {
		try {
			return new YouTube.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				GsonFactory.getDefaultInstance(),
				request -> {
				}
			).setApplicationName("youtube-search-app")
				.build();
		} catch (Exception e) {
			throw new RuntimeException("유튜브 API 클라이언트 생성에 실패했습니다.");
		}
	}
}

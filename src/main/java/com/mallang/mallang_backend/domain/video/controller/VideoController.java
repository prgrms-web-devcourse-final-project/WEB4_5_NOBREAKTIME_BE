package com.mallang.mallang_backend.domain.video.controller;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.service.VideoService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

	@Autowired
	private final VideoService videoService;

	/**
	 * Youtube ID 로 영상을 분석해 원어 자막, 번역 자막, 핵심 단어를 응답하는 메서드
	 * @param youtubeVideoId 유튜브 영상의 ID, ex) DF3KVSnyUWI
	 * @return 원어 자막, 번역 자막, 핵심 단어
	 */
	@GetMapping("/{youtubeVideoId}/analysis")
	public ResponseEntity<RsData<String>> videoAnalysis(@PathVariable String youtubeVideoId) {
		String result;
		try {
			result = videoService.analyzeVideo(YOUTUBE_VIDEO_BASE_URL + youtubeVideoId);
		} catch (IOException | InterruptedException e) {
			throw new ServiceException(AUDIO_DOWNLOAD_FAILED);
		}

		// TODO: Clova Speech 연결, Open AI 연결 기능 추가

		return ResponseEntity.ok(new RsData<>(
			"",
			"영상이 분석되었습니다.",
			result
		));
	}

	/**
	 * Clova Speech 에 음성 리소스를 제공하기 위한 메서드
	 * @param fileName 리소스 파일명
	 * @return 음성 리소스
	 */
	@GetMapping("/uploaded/{fileName}")
	public ResponseEntity<byte[]> getAudioFile(@PathVariable String fileName) {
		try {
			byte[] audioData = videoService.getAudioFile(fileName);
			// 오디오 파일 타입 설정
			String contentType = "audio/mpeg"; // 파일 형식에 맞게 설정 (예: .mp3는 audio/mpeg)
			// 파일 다운로드 시 파일명 설정
			return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(audioData);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // 파일을 찾을 수 없으면 404 반환
		}
	}
}

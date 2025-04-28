package com.mallang.mallang_backend.global.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class YoutubeAudioExtractorImpl implements YoutubeAudioExtractor {

	private static final Logger log = LoggerFactory.getLogger(YoutubeAudioExtractorImpl.class);
	private final ProcessRunner processRunner;

	@Override
	public String extractAudio(String youtubeUrl) throws IOException, InterruptedException {
		Process infoProcess = processRunner.runProcess(
			"yt-dlp",
			"--dump-json",
			youtubeUrl
		);

		StringBuilder jsonOutput = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(infoProcess.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				jsonOutput.append(line);
			}
		}

		int infoExitCode = infoProcess.waitFor();
		if (infoExitCode != 0) {
			throw new RuntimeException("yt-dlp 영상 정보 조회 실패. exitCode=" + infoExitCode);
		}

		// uration 파싱
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root = objectMapper.readTree(jsonOutput.toString());

		int durationSeconds = root.path("duration").asInt(-1);
		if (durationSeconds == -1) {
			throw new RuntimeException("영상 길이(duration)를 가져올 수 없습니다.");
		}

		if (durationSeconds >= 1200) { // 20분 = 600초
			throw new RuntimeException("20분 이상 영상은 다운로드할 수 없습니다. (" + (durationSeconds/60) + "분)");
		}

		// 다운로드 진행
		String outputPath = "/tmp/audio_" + UUID.randomUUID() + System.currentTimeMillis() + ".mp3";

		Process process = processRunner.runProcess(
			"yt-dlp",
			"-x", "--audio-format", "mp3",
			"-o", outputPath,
			youtubeUrl
		);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				log.info("[yt-dlp] {}", line);
			}
		}

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("yt-dlp 실행 실패. exitCode=" + exitCode);
		}

		return outputPath;
	}
}

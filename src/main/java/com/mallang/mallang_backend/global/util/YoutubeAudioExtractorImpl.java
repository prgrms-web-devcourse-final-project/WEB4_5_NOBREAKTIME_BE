package com.mallang.mallang_backend.global.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class YoutubeAudioExtractorImpl implements YoutubeAudioExtractor {

	private static final int VIDEO_LENGTH_LIMIT_SECONDS = 1200;
	private static final String AUDIO_OUTPUT_DIR = "/tmp/";
	private static final String AUDIO_FILE_PREFIX = "audio_";
	private static final String AUDIO_FILE_EXTENSION = ".mp3";

	private final ProcessRunner processRunner;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String extractAudio(String youtubeUrl) throws IOException, InterruptedException {
		JsonNode videoInfo = fetchVideoInfo(youtubeUrl);
		validateVideoDuration(videoInfo);

		String outputPath = generateOutputPath();
		downloadAudio(youtubeUrl, outputPath);

		return outputPath;
	}

	private JsonNode fetchVideoInfo(String youtubeUrl) throws IOException, InterruptedException {
		Process infoProcess = processRunner.runProcess("yt-dlp", "--dump-json", youtubeUrl);

		String jsonOutput = readProcessOutput(infoProcess);
		int exitCode = infoProcess.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("yt-dlp 영상 정보 조회 실패. exitCode=" + exitCode);
		}

		return objectMapper.readTree(jsonOutput);
	}

	private void validateVideoDuration(JsonNode videoInfo) {
		int durationSeconds = videoInfo.path("duration").asInt(-1);
		if (durationSeconds == -1) {
			throw new RuntimeException("영상 길이(duration)를 가져올 수 없습니다.");
		}

		if (durationSeconds >= VIDEO_LENGTH_LIMIT_SECONDS) {
			throw new RuntimeException("20분 이상 영상은 다운로드할 수 없습니다. (" + (durationSeconds / 60) + "분)");
		}
	}

	private void downloadAudio(String youtubeUrl, String outputPath) throws IOException, InterruptedException {
		Process downloadProcess = processRunner.runProcess(
			"yt-dlp",
			"-x", "--audio-format", "mp3",
			"-o", outputPath,
			youtubeUrl
		);

		logProcessOutput(downloadProcess);

		int exitCode = downloadProcess.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("yt-dlp 오디오 다운로드 실패. exitCode=" + exitCode);
		}
	}

	private String readProcessOutput(Process process) throws IOException {
		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
		}
		return output.toString();
	}

	private void logProcessOutput(Process process) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				log.info("[yt-dlp] {}", line);
			}
		}
	}

	private String generateOutputPath() {
		String fileName = AUDIO_FILE_PREFIX + UUID.randomUUID() + System.currentTimeMillis() + AUDIO_FILE_EXTENSION;
		return AUDIO_OUTPUT_DIR + fileName;
	}
}

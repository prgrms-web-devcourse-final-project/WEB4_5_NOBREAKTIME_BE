package com.mallang.mallang_backend.global.util.youtube;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class YoutubeAudioExtractorImpl implements YoutubeAudioExtractor {

	private final ProcessRunner processRunner;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String extractAudio(String youtubeUrl) throws IOException, InterruptedException {
		ensureUploadsDirectoryExists();

		JsonNode videoInfo = fetchVideoInfo(youtubeUrl);
		validateVideoDuration(videoInfo);

		String fileName = generateFileName();

		runAudioExtraction(youtubeUrl, UPLOADS_DIR + fileName);

		return fileName;
	}

	private void ensureUploadsDirectoryExists() {
		File uploadsDirectory = new File(UPLOADS_DIR);
		if (!uploadsDirectory.exists()) {
			boolean created = uploadsDirectory.mkdirs();
			if (!created) {
				throw new ServiceException(VIDEO_PATH_CREATION_FAILED);
			}
		}
	}

	private JsonNode fetchVideoInfo(String youtubeUrl) throws IOException, InterruptedException {
		Process infoProcess = processRunner.runProcess(
			"yt-dlp",
			"--dump-json",
			youtubeUrl
		);

		String jsonOutput = readProcessOutput(infoProcess);
		int exitCode = infoProcess.waitFor();
		if (exitCode != 0) {
			throw new ServiceException(VIDEO_RETRIEVAL_FAILED);
		}

		return objectMapper.readTree(jsonOutput);
	}

	private void validateVideoDuration(JsonNode videoInfo) {
		int durationSeconds = videoInfo.path("duration").asInt(-1);
		if (durationSeconds == -1) {
			throw new ServiceException(VIDEO_RETRIEVAL_FAILED);
		}
		if (durationSeconds >= VIDEO_LENGTH_LIMIT_SECONDS) {
			throw new ServiceException(VIDEO_LENGTH_EXCEED);
		}
	}

	private void runAudioExtraction(String youtubeUrl, String outputPath) throws IOException, InterruptedException {
		Process process = processRunner.runProcess(
			"yt-dlp",
			"-x", "--audio-format", "mp3",
			"-o", outputPath,
			youtubeUrl
		);

		logProcessOutput(process);

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new ServiceException(AUDIO_DOWNLOAD_FAILED);
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

	private String generateFileName() {
		return AUDIO_FILE_PREFIX + UUID.randomUUID() + System.currentTimeMillis() + AUDIO_FILE_EXTENSION;
	}
}

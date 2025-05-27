package com.mallang.mallang_backend.global.util.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class YoutubeAudioExtractorImpl implements YoutubeAudioExtractor {

	private final ProcessRunner processRunner;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${youtube.extractor.info-cmd}")
	private String infoCmd;

	@Value("${youtube.extractor.extract-cmd}")
	private String extractCmd;

	@Bulkhead(name = "audioExtraction", fallbackMethod = "extractFallback")
	@Override
	public String extractAudio(String youtubeUrl) throws IOException, InterruptedException {
		ensureUploadsDirectoryExists();

		JsonNode videoInfo = fetchVideoInfo(youtubeUrl);
		validateVideoDuration(videoInfo);

		String fileName = generateFileName();

		runAudioExtraction(youtubeUrl, UPLOADS_DIR + fileName);

		return fileName;
	}

	// 대기가 길어져도 슬롯이 안 풀리면 이곳이 호출됩니다.
	public String extractFallback(String url, BulkheadFullException ex) {
		throw new ServiceException(ErrorCode.TOO_MANY_CONCURRENT_AUDIO_EXTRACTIONS);
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
		List<String> cmd = new ArrayList<>(Arrays.asList(infoCmd.split("\\s+")));
		cmd.add(youtubeUrl);

		Process infoProcess = processRunner.runProcess(cmd.toArray(new String[0]));
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
		List<String> cmd = new ArrayList<>(Arrays.asList(extractCmd.split("\\s+")));
		cmd.add(outputPath);
		cmd.add(youtubeUrl);

		Process process = processRunner.runProcess(cmd.toArray(new String[0]));
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
				log.debug("[yt-dlp] {}", line);
			}
		}
	}

	private String generateFileName() {
		return AUDIO_FILE_PREFIX + UUID.randomUUID() + System.currentTimeMillis() + AUDIO_FILE_EXTENSION;
	}
}

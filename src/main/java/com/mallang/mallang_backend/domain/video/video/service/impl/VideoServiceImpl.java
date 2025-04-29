package com.mallang.mallang_backend.domain.video.video.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.video.service.VideoService;
import com.mallang.mallang_backend.global.util.YoutubeAudioExtractor;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

	private final YoutubeAudioExtractor youtubeAudioExtractor;

	@Override
	public String analyzeVideo(String videoUrl) throws IOException, InterruptedException {
		String fileName = youtubeAudioExtractor.extractAudio(videoUrl);

		// TODO: fileName 으로 리소스 링크 만들어서 Clova Speech 한테 넘겨주고 응답으로 스크립트를 받는다.

		// TODO: OpenAI로 핵심 단어 추출하고, 번역한다.

		return fileName;
	}

	@Override
	public byte[] getAudioFile(String fileName) throws IOException {
		Path path = Paths.get(UPLOADS_DIR, fileName);
		return Files.readAllBytes(path);
	}
}

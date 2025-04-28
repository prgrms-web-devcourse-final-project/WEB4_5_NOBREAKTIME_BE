package com.mallang.mallang_backend.global.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YoutubeAudioExtractorImplTest {

	@Mock
	private ProcessRunner processRunner;

	@InjectMocks
	private YoutubeAudioExtractorImpl youtubeAudioExtractor;

	@Test
	@DisplayName("성공 - 유튜브 링크로 음성 파일을 추출할 수 있다")
	void testExtractAudio() throws Exception {
		Process mockInfoProcess = mock(Process.class);
		Process mockDownloadProcess = mock(Process.class);

		// JSON output
		InputStream infoInputStream = new ByteArrayInputStream("{\"duration\":300}".getBytes());
		when(mockInfoProcess.getInputStream()).thenReturn(infoInputStream);
		when(mockInfoProcess.waitFor()).thenReturn(0);

		// 다운로드 과정
		InputStream downloadInputStream = new ByteArrayInputStream("다운로드 완료 로그".getBytes());
		when(mockDownloadProcess.getInputStream()).thenReturn(downloadInputStream);
		when(mockDownloadProcess.waitFor()).thenReturn(0);

		// ProcessRunner 가 프로세스를 리턴할지 순서대로 지정
		when(processRunner.runProcess(
			ArgumentMatchers.eq("yt-dlp"), ArgumentMatchers.eq("--dump-json"), anyString()
		)).thenReturn(mockInfoProcess);

		when(processRunner.runProcess(
			ArgumentMatchers.eq("yt-dlp"), ArgumentMatchers.eq("-x"), ArgumentMatchers.eq("--audio-format"),
			ArgumentMatchers.eq("mp3"), ArgumentMatchers.eq("-o"), anyString(), anyString()
		)).thenReturn(mockDownloadProcess);

		String youtubeUrl = "https://www.youtube.com/watch?v=test";

		String result = youtubeAudioExtractor.extractAudio(youtubeUrl);

		assertNotNull(result);
		assertTrue(result.contains("/tmp/audio_"));

		// 명령어가 제대로 호출됐는지 검증
		verify(processRunner).runProcess(
			"yt-dlp", "--dump-json", youtubeUrl
		);

		verify(processRunner).runProcess(
			eq("yt-dlp"),
			eq("-x"),
			eq("--audio-format"),
			eq("mp3"),
			eq("-o"),
			anyString(),
			eq(youtubeUrl)
		);

		verify(processRunner, times(1)).runProcess(
			"yt-dlp", "--dump-json", youtubeUrl
		);
	}

	@Test
	@DisplayName("실패 - 영상 길이가 20분을 초과하면 음성 추출에 실패한다")
	void testExtractAudio_durationOver20Minutes_shouldThrowException() throws Exception {
		Process mockInfoProcess = mock(Process.class);

		// JSON output: duration이 1300초 (20분 초과)
		InputStream infoInputStream = new ByteArrayInputStream("{\"duration\":1300}".getBytes());
		when(mockInfoProcess.getInputStream()).thenReturn(infoInputStream);
		when(mockInfoProcess.waitFor()).thenReturn(0);

		when(processRunner.runProcess(
			ArgumentMatchers.eq("yt-dlp"), ArgumentMatchers.eq("--dump-json"), anyString()
		)).thenReturn(mockInfoProcess);

		RuntimeException exception = assertThrows(RuntimeException.class, () ->
			youtubeAudioExtractor.extractAudio("https://www.youtube.com/watch?v=dummy")
		);

		assertTrue(exception.getMessage().contains("20분 이상 영상은 다운로드할 수 없습니다"));
	}
}
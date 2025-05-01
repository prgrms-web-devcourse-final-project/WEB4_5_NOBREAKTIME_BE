package com.mallang.mallang_backend.global.util.youtube;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static org.assertj.core.api.Assertions.*;
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

import com.mallang.mallang_backend.global.exception.ServiceException;

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
		assertTrue(result.contains(AUDIO_FILE_PREFIX));

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

		ServiceException exception = assertThrows(ServiceException.class, () ->
			youtubeAudioExtractor.extractAudio("https://www.youtube.com/watch?v=dummy")
		);

		assertTrue(exception.getMessageCode().contains("video.length.exceed"));
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 영상 링크는 음성 추출에 실패한다")
	void testExtractAudio_invalidVideoLink_shouldThrowException() throws Exception {
		Process mockInfoProcess = mock(Process.class);

		// 실패한 프로세스 시뮬레이션 (exit code != 0)
		InputStream errorStream = new ByteArrayInputStream("에러 발생".getBytes());
		when(mockInfoProcess.getInputStream()).thenReturn(errorStream);
		when(mockInfoProcess.waitFor()).thenReturn(1); // 실패 (exit code 1)

		when(processRunner.runProcess(
			eq("yt-dlp"), eq("--dump-json"), anyString()
		)).thenReturn(mockInfoProcess);

		ServiceException exception = assertThrows(ServiceException.class, () ->
			youtubeAudioExtractor.extractAudio("https://www.youtube.com/watch?v=invalid")
		);

		assertThat(exception.getMessageCode()).isEqualTo("video.retrieval.failed");
	}
}
package com.mallang.mallang_backend.global.util.clova;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 부하 테스트 - Clova speech api 요청을 Mock Server 연결로 대체한 테스트용 ClovaSpeechClientImpl
 * 외부 테스트 도구를 사용할 때 Mock Server 실행 후 연결
 */
@Slf4j
public class ClovaSpeechClientMockImpl implements ClovaSpeechClient {
	/**
	 * Clova STT(Mock Server)에 음성 파일로 자막을 요청합니다.
	 * 여기서 음성 파일은 실제로 업로드되지 않습니다.
	 *
	 * recognize media using a file (로컬 파일 업로드 후 음성 인식 요청)
	 * @param file required, the media file (필수 파라미터, 로컬 파일)
	 * @param nestRequestEntity optional (필수 파라미터가 아님)
	 * @return string (문자열 반환)
	 */
	@Override
	public String upload(File file, NestRequestEntity nestRequestEntity) {
		log.debug("[ClovaServiceMock] mock 서버에 요청 중...");

		// mock 서버 주소 및 엔드포인트
		String mockUrl = "http://localhost:8002";

		// 요청 바디 구성
		Map<String, Object> body = new HashMap<>();
		body.put("fileName", file != null ? file.getName() : "mock-file.wav");
		body.put("language", nestRequestEntity != null ? nestRequestEntity.getLanguage() : "en");

		return WebClient.create(mockUrl)
			.get()
			.uri("/mock-api/stt/result")
			.retrieve()
			.onStatus(
				status -> status.is4xxClientError() || status.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.flatMap(errorBody -> {
						log.error("[ClovaServiceMock] 호출 실패. 상태: {}, 응답: {}", clientResponse.statusCode(), errorBody);
						return Mono.error(new UnsupportedOperationException());
					})
			)
			.bodyToMono(String.class)
			.block(); // 동기식 호출
	}
}

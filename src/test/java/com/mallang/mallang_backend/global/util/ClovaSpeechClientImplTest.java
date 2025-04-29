
package com.mallang.mallang_backend.global.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mallang.mallang_backend.global.common.Language;

class ClovaSpeechClientImplTest {
	@Mock
	private CloseableHttpClient httpClient;

	@Mock
	private CloseableHttpResponse httpResponse;

	@Mock
	private HttpEntity httpEntity;

	@InjectMocks
	private ClovaSpeechClientImpl clovaSpeechClient;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		clovaSpeechClient = new ClovaSpeechClientImpl(httpClient);
	}

	@Test
	@DisplayName("로컬 음성 파일로 Clova Speech STT 변환 요청을 보낼 수 있다")
	void upload_success() throws Exception {
		File file = File.createTempFile("test", ".wav");
		file.deleteOnExit();

		NestRequestEntity requestEntity = new NestRequestEntity(Language.ENGLISH);

		when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContentLength()).thenReturn(100L);
		when(httpEntity.getContent()).thenReturn(
			new ByteArrayInputStream("{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8))
		);

		String response = clovaSpeechClient.upload(file, requestEntity);

		assertNotNull(response);
		assertTrue(response.contains("ok"));

		// 실제로 HttpPost가 제대로 설정되었는지
		ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
		verify(httpClient).execute(captor.capture());

		HttpPost capturedPost = captor.getValue();
		assertTrue(capturedPost.getURI().toString().contains("/recognizer/upload"));
		assertNotNull(capturedPost.getEntity());
	}
}
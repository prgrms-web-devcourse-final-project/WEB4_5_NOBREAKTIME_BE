package com.mallang.mallang_backend.global.util.clova;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Component
public class ClovaSpeechClientImpl implements ClovaSpeechClient {

	// Clova Speech secret key
	@Value("${clova.speech.secret}")
	private String secret ;

	// Clova Speech invoke URL
	@Value("${clova.speech.invoke_url}")
	private String invoke_url;

	private CloseableHttpClient httpClient;

	public ClovaSpeechClientImpl() {
		this.httpClient = HttpClients.createDefault();
	}

	// 테스트용 생성자 오버로딩
	public ClovaSpeechClientImpl(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	private Gson gson = new Gson();

	private Header[] createHeaders() {
		return new Header[] {
			new BasicHeader("Accept", "application/json"),
			new BasicHeader("X-CLOVASPEECH-API-KEY", secret),
		};
	}

	/**
	 *
	 * recognize media using a file (로컬 파일 업로드 후 음성 인식 요청)
	 * @param file required, the media file (필수 파라미터, 로컬 파일)
	 * @param nestRequestEntity optional (필수 파라미터가 아님)
	 * @return string (문자열 반환)
	 */
	@Override
	public String upload(File file, NestRequestEntity nestRequestEntity) {
		HttpPost httpPost = new HttpPost(invoke_url + "/recognizer/upload");
		httpPost.setHeaders(createHeaders());
		HttpEntity httpEntity = MultipartEntityBuilder.create()
			.addTextBody("params", gson.toJson(nestRequestEntity), ContentType.APPLICATION_JSON)
			.addBinaryBody("media", file, ContentType.MULTIPART_FORM_DATA, file.getName())
			.build();
		httpPost.setEntity(httpEntity);
		return execute(httpPost);
	}

	private String execute(HttpPost httpPost) {
		try (final CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			final HttpEntity entity = httpResponse.getEntity();
			return EntityUtils.toString(entity, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
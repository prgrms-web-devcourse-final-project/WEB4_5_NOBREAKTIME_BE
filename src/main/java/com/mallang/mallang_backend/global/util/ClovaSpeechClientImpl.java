package com.mallang.mallang_backend.global.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

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
	 * recognize media using URL (외부 파일 URL로 음성 인식 요청)
	 * @param url required, the media URL (필수 파라미터, 외부 파일 URL)
	 * @param nestRequestEntity optional (필수 파라미터가 아님)
	 * @return string (문자열 반환)
	 */
	public String url(String url, NestRequestEntity nestRequestEntity) {
		HttpPost httpPost = new HttpPost(invoke_url + "/recognizer/url");
		httpPost.setHeaders(createHeaders());
		Map<String, Object> body = new HashMap<>();
		body.put("url", url);
		body.put("language", nestRequestEntity.getLanguage());
		body.put("completion", nestRequestEntity.getCompletion());
		body.put("callback", nestRequestEntity.getCallback());
		body.put("userdata", nestRequestEntity.getCallback());
		body.put("wordAlignment", nestRequestEntity.getWordAlignment());
		body.put("fullText", nestRequestEntity.getFullText());
		body.put("forbiddens", nestRequestEntity.getForbiddens());
		body.put("boostings", nestRequestEntity.getBoostings());
		body.put("diarization", nestRequestEntity.getDiarization());
		body.put("sed", nestRequestEntity.getSed());
		HttpEntity httpEntity = new StringEntity(gson.toJson(body), ContentType.APPLICATION_JSON);
		httpPost.setEntity(httpEntity);
		return execute(httpPost);
	}

	/**
	 * recognize media using Object Storage (네이버 클라우드 플랫폼의 Object Storage 내 파일 URL로 음성 인식 요청)
	 * @param dataKey required, the Object Storage key (필수 파라미터, Object Storage 키 값)
	 * @param nestRequestEntity optional (필수 파라미터가 아님)
	 * @return string (문자열 반환)
	 */
	public String objectStorage(String dataKey, NestRequestEntity nestRequestEntity) {
		HttpPost httpPost = new HttpPost(invoke_url + "/recognizer/object-storage");
		httpPost.setHeaders(createHeaders());
		Map<String, Object> body = new HashMap<>();
		body.put("dataKey", dataKey);
		body.put("language", nestRequestEntity.getLanguage());
		body.put("completion", nestRequestEntity.getCompletion());
		body.put("callback", nestRequestEntity.getCallback());
		body.put("userdata", nestRequestEntity.getCallback());
		body.put("wordAlignment", nestRequestEntity.getWordAlignment());
		body.put("fullText", nestRequestEntity.getFullText());
		body.put("forbiddens", nestRequestEntity.getForbiddens());
		body.put("boostings", nestRequestEntity.getBoostings());
		body.put("diarization", nestRequestEntity.getDiarization());
		body.put("sed", nestRequestEntity.getSed());
		StringEntity httpEntity = new StringEntity(gson.toJson(body), ContentType.APPLICATION_JSON);
		httpPost.setEntity(httpEntity);
		return execute(httpPost);
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
package com.mallang.mallang_backend.global.util.clova;

import java.util.List;
import java.util.Map;

import com.mallang.mallang_backend.global.common.Language;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NestRequestEntity {
	private String language;
	//completion optional, sync/async (응답 결과 반환 방식(sync/async) 설정, 필수 파라미터 아님)
	private String completion = "sync";
	//optional, used to receive the analyzed results (분석된 결과 조회 용도, 필수 파라미터 아님)
	private String callback;
	//optional, any data (임의의 Callback URL 값 입력, 필수 파라미터 아님)
	private Map<String, Object> userdata;
	private Boolean wordAlignment = Boolean.TRUE;
	private Boolean fullText = Boolean.TRUE;
	//boosting object array (키워드 부스팅 객체 배열)
	private List<Boosting> boostings;
	//comma separated words (쉼표 구분 키워드)
	private String forbiddens;
	private Diarization diarization;
	private Sed sed;

	/**
	 * Clova Speech에 보낼 요청 설정을 생성합니다.
	 * @param language 텍스트 인식 언어
	 */
	public NestRequestEntity(Language language) {
		this.language = language.getLanguageCode();
	}
}

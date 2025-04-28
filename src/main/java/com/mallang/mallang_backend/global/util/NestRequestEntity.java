package com.mallang.mallang_backend.global.util;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NestRequestEntity {
	private String language = "en-US";
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
}

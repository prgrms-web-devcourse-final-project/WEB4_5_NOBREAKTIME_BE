package com.mallang.mallang_backend.global.util.clova;

import java.io.File;

public interface ClovaSpeechClient {
	/**
	 * recognize media using a file (로컬 파일 업로드 후 음성 인식 요청)
	 * @param file required, the media file (필수 파라미터, 로컬 파일)
	 * @param nestRequestEntity optional (필수 파라미터가 아님)
	 * @return string (문자열 반환)
	 */
	String upload(File file, NestRequestEntity nestRequestEntity);
}

package com.mallang.mallang_backend.global.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RsDataTest {

    @Test
    @DisplayName("RsData 생성자: code, msg, data 필드가 올바르게 설정된다")
    void constructor_setsFieldsCorrectly() {
        // given
        RsData<String> rsData = new RsData<>("200-1", "성공", "결과 데이터");

        // then
        assertThat(rsData.getCode()).isEqualTo("200-1");
        assertThat(rsData.getMsg()).isEqualTo("성공");
        assertThat(rsData.getData()).isEqualTo("결과 데이터");
    }

    @Test
    @DisplayName("RsData.getStatusCode: code에서 HTTP 상태 코드를 추출한다")
    void getStatusCode_extractsStatusCodeFromCode() {
        // given
        RsData<?> rsData = new RsData<>("404-2", "Not Found");

        // when
        int statusCode = rsData.getStatusCode();

        // then
        assertThat(statusCode).isEqualTo(404);
    }

    @Test
    @DisplayName("RsData with null data: data 필드가 null일 수 있다")
    void rsData_allowsNullData() {
        // when
        RsData<String> rsData = new RsData<>("500-1", "에러", null);

        // then
        assertThat(rsData.getData()).isNull();
        assertThat(rsData.getCode()).isEqualTo("500-1");
    }
}

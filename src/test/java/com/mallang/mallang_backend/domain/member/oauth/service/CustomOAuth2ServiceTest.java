package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.global.exception.custom.RetryableException;
import com.mallang.mallang_backend.global.resilience4j.CustomCircuitBreakerConfig;
import com.mallang.mallang_backend.global.resilience4j.CustomRetryConfigV2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.HttpServerErrorException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@Import(value = {CustomCircuitBreakerConfig.class, CustomRetryConfigV2.class})
class CustomOAuth2ServiceTest {

    @MockitoSpyBean
    ExternalOAuth2UserService externalService;

    /**
     * CircuitBreaker → Retry → (실제 메서드 실행)
     */
    @Test
    @Disabled("슬랙 알림이 울려서...")
    @DisplayName("loadUser → 서킷 브레이커 호출 이후 retry 시도")
    void t1(CapturedOutput out) throws Exception {
        //given
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "테스트 용도 에러"))
                .when(externalService)
                .loadUser(any(OAuth2UserRequest.class));

        //when
        OAuth2UserRequest fakeRequest = Mockito.mock(OAuth2UserRequest.class);

        //then 실제 호출: 예외 발생
        assertThatThrownBy(() -> externalService.loadUser(fakeRequest))
                .isInstanceOf(RetryableException.class); // 위와 같은 예외는 서킷 브레이커가 변경해서 던져줄 것

        assertThat(out.getOut()).contains("[서킷 브레이커] OAuth2 로그인 실패 - 소셜 서버 내부 오류(5xx)")
                .contains("[oauthUserLoginService][txId=N/A] 1번째 시도 실패 - 예외: RetryableException (500 테스트 용도 에러) - 다음 시도 대기시간: 500ms")
                .contains("[oauthUserLoginService][txId=N/A] 5번 시도 후 최종 실패 - 발생 예외 목록:");
    }
}
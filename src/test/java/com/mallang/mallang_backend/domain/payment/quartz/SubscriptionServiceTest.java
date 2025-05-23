package com.mallang.mallang_backend.domain.payment.quartz;

import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionQueryRepository;
import com.mallang.mallang_backend.domain.subscription.service.SubscriptionService;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.SUBSCRIPTION_STATUS_UPDATE_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
public class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @MockitoBean
    private SubscriptionQueryRepository subscriptionQueryRepository;

    @Test
    void t1(CapturedOutput output) throws Exception {
        //given: mock 데이터 세팅
        List<Long> ids = new ArrayList<>(List.of(1L, 2L));
        when(subscriptionQueryRepository.findActiveSubWithMember()).thenReturn(ids);
        when(subscriptionQueryRepository.bulkUpdateStatus(ids)).thenReturn(2L);

        //when
        subscriptionService.updateSubscriptionStatus();

        //then
        verify(subscriptionQueryRepository).findActiveSubWithMember();
        verify(subscriptionQueryRepository).bulkUpdateStatus(ids);

        // 로그 확인
        assertThat(output.getOut()).contains("[구독만료성공] 2건 처리 완료");
    }

    @Test
    @DisplayName("FallBack - 재시도 하지 않는 오류 처리가 제대로 진행되는지 테스트")
    void updateSubscriptionStatusFallback(CapturedOutput output) throws Exception {
        //given: queryRepository 에서 예외 발생 설정
        when(subscriptionQueryRepository.findActiveSubWithMember())
                .thenThrow(new DataIntegrityViolationException("error"));

        //when: 서비스 메서드 실행 (예외 발생 예상)
        ServiceException exception = assertThrows(ServiceException.class,
                () -> subscriptionService.updateSubscriptionStatus());

        //then: Fallback 검증
        assertThat(exception.getErrorCode()).isEqualTo(SUBSCRIPTION_STATUS_UPDATE_FAILED);
        assertThat(output.getOut()).contains("무시된 예외 발생");
        assertThat(output.getOut()).contains("[구독만료실패] error");
    }

    @Test
    @DisplayName("Retry - 재시도 허용 예외 발생 시 제대로 진행되는지 테스트 / 성공")
    void updateSubscriptionStatusRetry(CapturedOutput output) throws Exception {
        //given: Mock 설정 -> 처음 2번은 실패, 3번째 성공
        when(subscriptionQueryRepository.findActiveSubWithMember())
                .thenThrow(new TransientDataAccessResourceException("네트워크 장애"))
                .thenThrow(new TransientDataAccessResourceException("네트워크 장애"))
                .thenReturn(List.of(1L, 2L, 3L));

        //when
        subscriptionService.updateSubscriptionStatus();

        //then: 3번 호출 되었는가
        verify(subscriptionQueryRepository, times(3)).findActiveSubWithMember();
        // bulkUpdateStatus가 1번 호출되었는지 확인
        verify(subscriptionQueryRepository, times(1)).bulkUpdateStatus(anyList());

        assertThat(output.getOut()).contains("[Retry]");
        assertThat(output.getOut()).contains("[구독만료성공] 0건 처리 완료");
        assertThat(output.getOut()).doesNotContain("[Fallback]");
    }

    @Test
    @DisplayName("Fallback - 재시도 허용 예외 발생 시 제대로 진행되는지 테스트 / 실패")
    void updateSubscriptionStatusRetryFail(CapturedOutput output) throws Exception {
        //given: Mock 설정 -> 3번 다 실패
        when(subscriptionQueryRepository.findActiveSubWithMember())
                .thenThrow(new TransientDataAccessResourceException("네트워크 장애"))
                .thenThrow(new TransientDataAccessResourceException("네트워크 장애"))
                .thenThrow(new TransientDataAccessResourceException("네트워크 장애"));

        //when
        assertThrows(ServiceException.class, () -> subscriptionService.updateSubscriptionStatus());

        //then: 3번 호출 되었는가
        verify(subscriptionQueryRepository, times(3)).findActiveSubWithMember();
        verify(subscriptionQueryRepository, never()).bulkUpdateStatus(anyList());

        assertThat(output.getOut()).contains("[구독만료실패] 네트워크 장애");
        assertThat(output.getOut()).contains("3번 시도 후 최종 실패 - 발생 예외 목록:");
    }
}


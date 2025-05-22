package com.mallang.mallang_backend.domain.payment.service.process.complete;

import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.payment.dto.approve.PaymentResponse;
import com.mallang.mallang_backend.domain.payment.repository.PaymentQueryRepository;
import com.mallang.mallang_backend.domain.payment.service.confirm.PaymentConfirmService;
import com.mallang.mallang_backend.domain.payment.service.process.error.HandleErrorService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.NOT_FOUND_MEMBER_GRANTED_INFO;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompletePayServiceImpl implements CompletePayService {

    private final PaymentQueryRepository paymentQueryRepository;
    private final PaymentConfirmService paymentConfirmService;
    private final HandleErrorService errorService;

    /**
     * 결제 결과를 처리하고, 멤버 권한 정보를 반환합니다.
     *
     * @param response 결제 응답 객체
     * @return MemberGrantedInfo 멤버 권한 정보
     * @throws ServiceException 멤버 권한 정보가 없거나 잘못된 경우 발생
     */
    @Transactional
    @Override
    @Retry(name = "dataSaveInstance", fallbackMethod = "completePaymentFallback" )
    public MemberGrantedInfo completePayment(PaymentResponse response) {
        // 1. 결제 결과 처리 (트랜잭션 내)
        paymentConfirmService.processPaymentResult(response);

        // 2. 멤버 권한 정보 조회 (트랜잭션 내)
        MemberGrantedInfo info = paymentQueryRepository
                .findMemberGrantedInfoWithRole(response.getOrderId());
        if (info == null || info.memberId() == null || info.type() == null) {
            throw new ServiceException(NOT_FOUND_MEMBER_GRANTED_INFO);
        }

        return info;
    }

    /**
     * 결제 처리 실패 시 호출되는 fallback 메서드입니다.
     *
     * @param response 결제 응답 객체
     * @throws ServiceException 예외 처리
     */
    public MemberGrantedInfo completePaymentFallback(PaymentResponse response,
                                                     Throwable t) {
        log.error("[결제 결과 처리 실패] orderId: {} | errorCode: {} | message: {}",
                response.getOrderId(), t.getCause(), t.getMessage());

        // 결제 취소 로직 실행
        errorService.handleSaveFailedFallback(response.getOrderId());

        throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_RESULT_SAVED_FAILED, t);
    }

    public record MemberGrantedInfo(Long memberId, SubscriptionType type) {
    }
}

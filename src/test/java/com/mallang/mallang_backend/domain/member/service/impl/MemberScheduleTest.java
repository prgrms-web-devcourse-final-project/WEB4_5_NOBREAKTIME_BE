package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("local")
@SpringBootTest
@EnableRetry
@ExtendWith(MockitoExtension.class)
public class MemberScheduleTest {

    @MockitoBean
    private MemberQueryRepository memberQueryRepository;

    @Autowired
    private MemberServiceImpl memberService;

    @Test
    void 재시도_로직_검증() {
        // Given: TransientDataAccessException 발생
        doThrow(new TransientDataAccessException("DB 연결 실패") {})
                .when(memberQueryRepository).bulkDeleteExpiredMembers(any());

        // When & Then: 예외 발생 확인
        assertThatThrownBy(() -> memberService.scheduleAccountDeletion())
                .isInstanceOf(TransientDataAccessException.class);

        // Then: 3회 재시도 검증
        verify(memberQueryRepository, times(3))
                .bulkDeleteExpiredMembers(any());
    }
}

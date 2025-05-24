package com.mallang.mallang_backend.domain.member.service.withdrawn;

import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLTransientConnectionException;

public interface MemberWithdrawalService {
    void withdrawMember(Long memberId);

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000),
            include = {TransientDataAccessException.class, SQLTransientConnectionException.class},
            exclude = {IllegalArgumentException.class, DataIntegrityViolationException.class}
    )
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void scheduleAccountDeletion();

    boolean existsByOriginalPlatformId(String platformId);

    WithdrawnLog findByOriginalPlatformId(String platformId);
}
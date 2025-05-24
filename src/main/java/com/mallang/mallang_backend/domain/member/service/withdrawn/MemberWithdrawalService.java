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
    boolean existsByOriginalPlatformId(String platformId);

    WithdrawnLog findByOriginalPlatformId(String platformId);
}
package com.mallang.mallang_backend.domain.member.service.withdrawn;

import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

public interface MemberWithdrawalService {
    void withdrawMember(Long memberId);
    void scheduleAccountDeletion();

    @Transactional
    boolean handleRejoinScenario(HttpServletResponse response, String platformId);

    boolean existsByOriginalPlatformId(String platformId);
    WithdrawnLog findByOriginalPlatformId(String platformId);
}
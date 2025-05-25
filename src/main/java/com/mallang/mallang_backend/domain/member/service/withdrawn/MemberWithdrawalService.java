package com.mallang.mallang_backend.domain.member.service.withdrawn;

import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;

public interface MemberWithdrawalService {
    void withdrawMember(Long memberId);
    void scheduleAccountDeletion();
    boolean existsByOriginalPlatformId(String platformId);
    WithdrawnLog findByOriginalPlatformId(String platformId);
}
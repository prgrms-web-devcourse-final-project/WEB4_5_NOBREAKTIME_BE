package com.mallang.mallang_backend.domain.member.service.withdrawn;

public interface MemberWithdrawalService {
    void withdrawMember(Long memberId);
    void scheduleAccountDeletion();
}
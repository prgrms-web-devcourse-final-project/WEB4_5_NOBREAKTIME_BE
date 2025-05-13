package com.mallang.mallang_backend.domain.member.service.valid;

public interface MemberValidationService {
    void validateEmailNotDuplicated(String email);
    boolean isNicknameAvailable(String nickname);
    boolean existsByPlatformId(String platformId);
}

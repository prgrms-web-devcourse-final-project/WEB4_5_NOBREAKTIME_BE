package com.mallang.mallang_backend.domain.member.service.profile;

import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberProfileService {
    UserProfileResponse getUserProfile(Long memberId);
    String changeProfile(Long memberId, MultipartFile file);
    void deleteOldProfileImage(Long memberId);
}
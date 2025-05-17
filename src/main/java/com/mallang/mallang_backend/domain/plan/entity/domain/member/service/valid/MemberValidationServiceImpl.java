package com.mallang.mallang_backend.domain.plan.entity.domain.member.service.valid;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mallang.mallang_backend.global.exception.ErrorCode.DUPLICATE_FILED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberValidationServiceImpl implements MemberValidationService {

    private final MemberRepository memberRepository;

    /**
     * 이메일 중복 여부를 검사합니다.
     * 이미 존재하는 이메일일 경우 ServiceException 을 발생시킵니다.
     *
     * @param email 검사할 이메일 주소
     * @throws ServiceException 중복된 이메일인 경우 발생
     */
    @Override
    public void validateEmailNotDuplicated(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ServiceException(DUPLICATE_FILED);
        }
    }

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     *
     * @param nickname 확인할 닉네임
     * @return 닉네임이 존재하지 않을 경우 true, 존재할 경우 false
     */
    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    /**
     * 플랫폼 ID로 회원 존재 여부를 확인합니다.
     *
     * @param platformId 확인할 플랫폼 ID
     * @return 회원이 존재할 경우 true, 존재하지 않을 경우 false
     */
    @Override
    public boolean existsByPlatformId(String platformId) {
        return memberRepository.findByPlatformId(platformId).isPresent();
    }
}

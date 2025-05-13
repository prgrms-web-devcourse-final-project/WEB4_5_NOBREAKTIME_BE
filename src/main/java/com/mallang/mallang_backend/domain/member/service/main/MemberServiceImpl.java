package com.mallang.mallang_backend.domain.member.service.main;

import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.sub.SubscriptionService;
import com.mallang.mallang_backend.domain.member.service.profile.MemberProfileService;
import com.mallang.mallang_backend.domain.member.service.valid.MemberValidationService;
import com.mallang.mallang_backend.domain.member.service.withdrawn.MemberWithdrawalService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.mallang.mallang_backend.global.common.Language.NONE;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

/**
 * 쓰기 작업(등록, 수정, 삭제 등)은 별도로 @Transactional 붙여 주세요
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;

    private final MemberProfileService profileService;
    private final MemberWithdrawalService withdrawalService;
    private final MemberValidationService validationService;
    private final SubscriptionService subscriptionService;

    // memberService -> 단순 조립 + 사용
    @Override
    public UserProfileResponse getUserProfile(Long memberId) {
        return profileService.getUserProfile(memberId);
    }

    @Override
    public void deleteOldProfileImage(Long memberId) {
        profileService.deleteOldProfileImage(memberId);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return validationService.isNicknameAvailable(nickname);
    }

    @Override
    public void validateEmailNotDuplicated(String email) {
        validationService.validateEmailNotDuplicated(email);
    }

    @Override
    public String getRoleName(Long memberId) {
        return subscriptionService.getRoleName(memberId);
    }

    @Override
    public boolean existsByPlatformId(String platformId) {
        return validationService.existsByPlatformId(platformId);
    }

    @Override
    public void withdrawMember(Long memberId) {
        withdrawalService.withdrawMember(memberId);
    }

    @Override
    public void scheduleAccountDeletion() {
        withdrawalService.scheduleAccountDeletion();
    }

    @Override
    public String changeProfile(Long memberId, MultipartFile file) {
        return profileService.changeProfile(memberId, file);
    }

    /**
     * 플랫폼 ID로 회원 정보를 조회합니다.
     *
     * @param platformId 조회할 회원의 플랫폼 ID
     * @return 플랫폼 ID에 해당하는 회원 객체
     * @throws ServiceException 회원을 찾을 수 없을 경우 {@code MEMBER_NOT_FOUND} 예외 발생
     */
    @Override
    public Member getMemberByPlatformId(String platformId) {
        return memberRepository.findByPlatformId(platformId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
    }

    // 소셜 로그인 회원 멤버 가입
    @Transactional
    public Long signupByOauth(String platformId,
                              String email,
                              String nickname,
                              String profileImage,
                              LoginPlatform loginPlatform) {

        Member member = Member.builder()
                .platformId(platformId) // null 불가능
                .email(email) // null 가능
                .password(null)
                .nickname(nickname)
                .loginPlatform(loginPlatform)
                .language(NONE)
                .profileImageUrl(profileImage).build();

        Member savedMember = memberRepository.save(member);

        // 회원가입 시 언어별 기본 단어장 생성
        List<Wordbook> defaultWordbooks = Wordbook.createDefault(savedMember);
        wordbookRepository.saveAll(defaultWordbooks);

        // 회원가입 시 언어별 기본 표현함 생성
        List<ExpressionBook> defaultBooks = ExpressionBook.createDefault(member);
        expressionBookRepository.saveAll(defaultBooks);

        return savedMember.getId();
    }

    /**
     * 회원의 학습 언어를 설정합니다.
     * 이미 언어가 설정된 경우 예외를 발생시킵니다.
     * - 최초 설정 시에만 설정이 가능하도록
     *
     * @param memberId 언어를 설정할 회원의 ID
     * @param language 설정할 언어
     * @throws ServiceException 이미 언어가 설정된 경우 발생
     */
    @Transactional
    public void updateLearningLanguage(Long memberId, Language language) {
        Member member = findMemberOrThrow(memberId);

        if (member.getLanguage() != Language.NONE) {
            throw new ServiceException(ErrorCode.LANGUAGE_ALREADY_SET);
        }

        member.updateLearningLanguage(language);
    }

    /**
     * 회원 정보를 변경합니다.
     * 닉네임과 이메일의 중복 여부를 확인 후 업데이트를 수행합니다. (2차 검증)
     *
     * @param memberId 변경할 회원 ID
     * @param request  변경할 닉네임 및 이메일 정보
     * @return 변경 완료된 회원 정보 DTO
     */
    @Override
    @Transactional
    public ChangeInfoResponse changeInformation(Long memberId, ChangeInfoRequest request) {
        Member member = findMemberOrThrow(memberId);
        validateEmailNotDuplicated(request.getEmail());

        if (isNicknameAvailable(request.getNickname())) {
            member.updateNickname(request.getNickname());
            member.updateEmail(request.getEmail());
        }

        return new ChangeInfoResponse(request.getNickname(), request.getEmail());
    }

    /**
     * 회원을 조회하고, 없으면 예외를 발생
     *
     * @param memberId 회원 ID
     * @return 회원 엔티티
     */
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
    }

    public Member getMemberById(Long memberId) {
        return findMemberOrThrow(memberId);
    }
}

package com.mallang.mallang_backend.domain.member.service.main;

import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.SignupRequest;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
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
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

/**
 * 쓰기 작업(등록, 수정, 삭제 등)은 별도로 @Transactional 붙여 주세요
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileService profileService;
    private final MemberWithdrawalService withdrawalService;
    private final MemberValidationService validationService;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;

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
    @Transactional
    public void withdrawMember(Long memberId) {
        withdrawalService.withdrawMember(memberId);
    }

    @Override
    @Transactional
    public String changeProfile(Long memberId, MultipartFile file) {
        return profileService.changeProfile(memberId, file);
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
    public ChangeInfoResponse changeInformation(Long memberId,
                                                ChangeInfoRequest request
    ) {
        Member member = findMemberOrThrow(memberId);

        // 닉네임 변경
        if (isNicknameAvailable(request.getNickname())) {
            member.updateNickname(request.getNickname());
        }

        // 언어 설정 변경
        if (member.getLanguage() != request.getLanguage()) {
            member.updateLanguage(request.getLanguage());
        }

        return new ChangeInfoResponse(
                request.getNickname(),
                request.getLanguage()
        );
    }

    public Member getMemberById(Long memberId) {
        return findMemberOrThrow(memberId);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * OAuth 제공자로부터 받은 정보를 기반으로 회원가입을 처리합니다.
     * <p>
     * 기본 언어별 단어장과 표현함을 자동 생성하며, 트랜잭션 범위 내에서 모든 작업을 수행합니다.
     *
     * @param request 사용자 가입 요청 DTO (platformId, email, nickname, profileImage, loginPlatform 포함)
     */
    @Transactional
    @Override
    public void signupByOauth(SignupRequest request) {

        Member newMember = buildMemberFromRequest(request);
        Member savedMember = memberRepository.save(newMember);

        createDefaultWordbooks(savedMember);
        createDefaultExpressionBooks(savedMember);
    }

    // == 내부 헬퍼 메서드 == //
    private Member buildMemberFromRequest(SignupRequest request) {
        return Member.builder()
                .platformId(request.platformId())
                .email(request.email())
                .nickname(request.nickname())
                .loginPlatform(request.loginPlatform())
                .language(NONE)
                .profileImageUrl(request.profileImage())
                .build();
    }

    private void createDefaultWordbooks(Member member) {
        List<Wordbook> defaultWordbooks = Wordbook.createDefault(member);
        wordbookRepository.saveAll(defaultWordbooks);
    }

    private void createDefaultExpressionBooks(Member member) {
        List<ExpressionBook> defaultBooks = ExpressionBook.createDefault(member);
        expressionBookRepository.saveAll(defaultBooks);
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
}

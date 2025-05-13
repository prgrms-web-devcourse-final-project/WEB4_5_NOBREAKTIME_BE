package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.member.service.SubscriptionService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLTransientConnectionException;
import java.time.LocalDateTime;
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

    private final S3ImageUploader imageUploader;
    private final MemberRepository memberRepository;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;
    private final SubscriptionService subscriptionService;
    private final MemberQueryRepository memberQueryRepository;

    /**
     * email 로 Member 조회
     *
     * @param email (로그인 시 이용하는 ID 값)
     * @return Member 객체
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() ->
                new ServiceException(MEMBER_NOT_FOUND));
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
     * 주어진 회원 ID로 사용자 프로필 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return UserProfileResponse 사용자 프로필 응답 DTO
     * @throws ServiceException 회원이 존재하지 않을 경우 발생
     */
    @Override
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = findMemberOrThrow(memberId);

        return UserProfileResponse.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImage(member.getProfileImageUrl())
                .subscriptionType(member.getSubscriptionType())
                .language(member.getLanguage())
                .build();
    }

    /**
     * member 에 접근해서 구독 정보를 가져 오기
     *
     * @param memberId
     * @return member 의 구독 타입에서 가져온 권한 정보
     */
    public String getRoleName(Long memberId) {
        return findMemberOrThrow(memberId).getSubscriptionType().getRoleName();
    }

    public Member getMemberById(Long memberId) {
        return findMemberOrThrow(memberId);
    }

    /**
     * 회원 탈퇴 처리
     * - 활성 구독 존재 시 BASIC 등급으로 다운그레이드
     * - 회원 탈퇴 일자를 withdrawalDate 에 추가 후 개인정보 마스킹
     *
     * @param memberId 대상 회원 ID
     */
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        deleteOldProfileImage(member);

        if (subscriptionService.hasActiveSubscription(memberId)) {
            downgradeSubscriptionToBasic(memberId);
        }

        member.markAsWithdrawn();
    }

    private void downgradeSubscriptionToBasic(Long memberId) {
        subscriptionService.updateSubscription(memberId, SubscriptionType.BASIC);
    }

    /**
     * 6개월 이상 경과한 탈퇴 회원 일괄 삭제
     * - 매일 새벽 3시 실행
     * - 일시적 오류 발생(DB 연결, 네트워크 오류) 시 최대 3회 재시도 (5초 간격)
     * - 비즈니스 로직 오류 (외래키 제약 조건 등): 재시도 의미 없어 실행하지 않음
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000),
            include = {TransientDataAccessException.class, SQLTransientConnectionException.class},
            exclude = {IllegalArgumentException.class, DataIntegrityViolationException.class}
    )
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleAccountDeletion() {
        LocalDateTime deletionThreshold = LocalDateTime.now().minusMonths(6);
        long deletedCount = memberQueryRepository.bulkDeleteExpiredMembers(deletionThreshold);

        log.info("탈퇴 완료 후 6개월 경과 회원 삭제 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 회원의 프로필 이미지를 변경합니다.
     *
     * @param memberId 프로필을 변경할 회원의 ID
     * @param file     새로 업로드할 프로필 이미지 파일
     * @return 변경된 프로필 이미지의 URL
     * @throws ServiceException 파일이 비어있거나 지원하지 않는 타입일 경우, 회원이 존재하지 않을 경우 발생
     */
    @Override
    @Transactional
    public String changeProfile(Long memberId,
                                MultipartFile file) {

        validateImageFile(file);

        Member member = findMemberOrThrow(memberId);

        deleteOldProfileImage(member);

        String newImageUrl = uploadNewProfileImage(file);
        member.updateProfileImageUrl(newImageUrl);

        return newImageUrl;
    }

    /**
     * 이미지 파일의 유효성을 검사합니다.
     *
     * @param file 업로드된 파일
     * @throws ServiceException 파일이 비어있거나 지원하지 않는 타입일 경우 발생
     */
    private void validateImageFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ServiceException(FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException(NOT_SUPPORTED_TYPE);
        }
    }

    /**
     * 기존 프로필 이미지를 삭제합니다.
     *
     * @param member 대상 회원 엔티티
     */
    private void deleteOldProfileImage(Member member) {

        String oldProfileUrl = member.getProfileImageUrl();
        if (oldProfileUrl != null && !oldProfileUrl.isBlank()) {
            imageUploader.deleteObjectByUrl(oldProfileUrl);
        }
    }

    /**
     * 새 이미지를 업로드하고 URL을 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL
     */
    private String uploadNewProfileImage(MultipartFile file) {
        ImageUploadRequest request = new ImageUploadRequest(file);
        return imageUploader.uploadImageURL(request);
    }


    /**
     * 이메일 중복 여부를 검사합니다.
     * 이미 존재하는 이메일일 경우 ServiceException 을 발생시킵니다.
     *
     * @param email 검사할 이메일 주소
     * @throws ServiceException 중복된 이메일인 경우 발생
     */
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
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    /**
     * 플랫폼 ID로 회원 존재 여부를 확인합니다.
     *
     * @param platformId 확인할 플랫폼 ID
     * @return 회원이 존재할 경우 true, 존재하지 않을 경우 false
     */
    public boolean existsByPlatformId(String platformId) {
        return memberRepository.findByPlatformId(platformId).isPresent();
    }

    /**
     * 회원 정보를 변경합니다.
     * 닉네임과 이메일의 중복 여부를 확인 후 업데이트를 수행합니다. (2차 검증)
     *
     * @param memberId 변경할 회원 ID
     * @param request 변경할 닉네임 및 이메일 정보
     * @return 변경 완료된 회원 정보 DTO
     */
    @Override
    @Transactional
    public ChangeInfoResponse changeInformation(Long memberId, ChangeInfoRequest request) {
        Member member = findMemberOrThrow(memberId);

        // 이메일이 변경된 경우에만 중복 체크
        if (!member.getEmail().equals(request.getEmail())) {
            validateEmailNotDuplicated(request.getEmail());
            member.updateEmail(request.getEmail());
        }

        // 닉네임이 변경된 경우에만 중복 체크
        if (!member.getNickname().equals(request.getNickname())) {
            if (!isNicknameAvailable(request.getNickname())) {
                throw new ServiceException(DUPLICATE_FILED);
            }
            member.updateNickname(request.getNickname());
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
}

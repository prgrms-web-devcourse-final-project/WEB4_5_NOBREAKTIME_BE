package com.mallang.mallang_backend.domain.member.service.impl;

import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.Subscription;
import com.mallang.mallang_backend.domain.member.query.MemberQueryRepository;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.domain.member.service.SubscriptionService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
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
import static com.mallang.mallang_backend.global.exception.ErrorCode.FILE_EMPTY;
import static com.mallang.mallang_backend.global.exception.ErrorCode.NOT_SUPPORTED_TYPE;

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

    // 이메일로 멤버가 존재하는지 확인
    public Boolean isExistEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

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

    // 소셜 로그인 회원 멤버 가입
    @Transactional
    public Long signupByOauth(String id, String nickname, String profileImage, LoginPlatform loginPlatform) {

        Member member = Member.builder()
                .email(id)
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

    // 소셜 로그인 회원 언어 정보 추가
    @Transactional
    public void updateLearningLanguage(Long memberId, Language language) {
        findMemberOrThrow(memberId).updateLearningLanguage(language);
    }

    /**
     * member 에 접근해서 구독 정보를 가져 오기
     *
     * @param memberId
     * @return member 의 구독 타입에서 가져온 권한 정보
     */
    public String getRoleName(Long memberId) {
        return findMemberOrThrow(memberId).getSubscription().getRoleName();
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
        subscriptionService.updateSubscription(memberId, Subscription.BASIC);
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

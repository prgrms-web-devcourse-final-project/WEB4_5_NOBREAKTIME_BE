package com.mallang.mallang_backend.domain.member.oauth.service;

import com.mallang.mallang_backend.domain.member.dto.ImageUploadRequest;
import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLogRepository;
import com.mallang.mallang_backend.domain.member.oauth.processor.OAuth2UserProcessor;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.util.s3.S3ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.mallang.mallang_backend.global.common.Language.NONE;
import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.CANNOT_SIGNUP_WITH_THIS_ID;
import static com.mallang.mallang_backend.global.exception.ErrorCode.NICKNAME_GENERATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final S3ImageUploader imageUploader;
    private final WithdrawnLogRepository logRepository;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;

    /**
     * 신규 회원을 등록합니다.
     *
     * @param platform       로그인 플랫폼 정보
     * @param userAttributes 사용자 속성 정보 (플랫폼에서 전달)
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우 예외 발생
     */
    @Transactional
    public void registerNewMember(LoginPlatform platform,
                                  Map<String, Object> userAttributes) {

        // 필수 속성 추출
        String platformId = (String) userAttributes.get(PLATFORM_ID_KEY);

        // 30일 이내 탈퇴 이력이 존재하면 예외 발생
        validateWithdrawnLogNotRejoinable(platformId);

        String email = (String) userAttributes.get("email");
        String originalNickname = (String) userAttributes.get(NICKNAME_KEY);
        String profileImage = (String) userAttributes.get(PROFILE_IMAGE_KEY);

        // 닉네임 중복 방지 로직 적용
        String nickname = generateUniqueNickname(originalNickname);

        log.debug("사용자 platformId: {}, email: {}, nickname: {}, profileImage: {}",
                platformId, email, nickname, profileImage);

        // 프로필 이미지 S3 업로드
        String s3ProfileImageUrl = uploadProfileImage(profileImage);

        signupByOauth(
                platformId,
                email,
                originalNickname,
                s3ProfileImageUrl,
                platform
        );
    }

    private void signupByOauth(String platformId,
                               String email,
                               String nickname,
                               String profileImage,
                               LoginPlatform loginPlatform) {

        Member member = Member.builder()
                .platformId(platformId) // null 불가능
                .email(email) // null 가능
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
    }

    /**
     * 30일 이내 탈퇴 이력이 있는 경우 가입을 제한합니다.
     *
     * @param platformId 플랫폼 고유 아이디
     * @throws ServiceException 30일 이내 탈퇴 이력이 있을 경우
     */
    private void validateWithdrawnLogNotRejoinable(String platformId) {

        if (logRepository.existsByOriginalPlatformId(platformId)) {

            WithdrawnLog withdrawnLog = logRepository.findByOriginalPlatformId(platformId);
            LocalDateTime rejoinAvailableAt = withdrawnLog.getCreatedAt().plusDays(30); // 가입 가능 날짜

            if (rejoinAvailableAt.isAfter(LocalDateTime.now())) {
                log.error("아직 가입할 수 없는 회원: {}", platformId);
                throw new ServiceException(CANNOT_SIGNUP_WITH_THIS_ID);
            }
        }
    }

    /**
     * 프로필 이미지를 S3에 업로드하고 URL을 반환합니다.
     *
     * @param profileImageUrl 업로드할 이미지 URL
     * @return S3에 업로드된 이미지 URL
     */
    private String uploadProfileImage(String profileImageUrl) {
        ImageUploadRequest request = new ImageUploadRequest(profileImageUrl);
        return imageUploader.uploadImageURL(request);
    }

    /**
     * 원본 닉네임을 기반으로 고유한 닉네임을 생성합니다.
     * - 중복 시 랜덤 접미사(2~3자리)를 추가
     * - 최대 5회 시도 후 예외 발생
     *
     * @param originalNickname 사용자가 입력한 원본 닉네임
     * @return 사용 가능한 고유 닉네임
     * @throws ServiceException 유일한 닉네임 생성 실패 시
     */
    private String generateUniqueNickname(String originalNickname) {
        final int MAX_ATTEMPTS = 5;
        String currentNickname = originalNickname;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (!memberRepository.existsByNickname(currentNickname)) {
                return currentNickname;
            }
            String randomSuffix = RandomStringGenerator.generate(2 + new SecureRandom().nextInt(2));
            currentNickname = originalNickname + randomSuffix;
        }

        log.error("닉네임 생성 실패: {}", originalNickname);
        throw new ServiceException(NICKNAME_GENERATION_FAILED);
    }
}

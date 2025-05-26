package com.mallang.mallang_backend.domain.member.controller;

import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.log.withdrawn.WithdrawnLog;
import com.mallang.mallang_backend.domain.member.service.main.MemberService;
import com.mallang.mallang_backend.domain.member.service.withdrawn.MemberWithdrawalService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import com.mallang.mallang_backend.global.token.TokenService;
import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.mallang.mallang_backend.global.constants.AppConstants.ACCESS_TOKEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Tag(name = "Member", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenService tokenService;
    private final MemberWithdrawalService withdrawalService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * @param userDetails 로그인 사용자 정보
     * @param language    변경할 언어
     */
    @Operation(summary = "사용자 학습 언어 변경", description = "로그인한 사용자의 학습 언어를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "언어 설정이 완료되었습니다.")
    @PossibleErrors({MEMBER_NOT_FOUND, LANGUAGE_ALREADY_SET})
    @PatchMapping("/update-language")
    public ResponseEntity<RsData<Void>> updateUserLanguage(
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails,
            @Parameter(description = "변경할 언어", required = true, example = "ENGLISH")
            @RequestParam("language") Language language
    ) {
        memberService.updateLearningLanguage(userDetails.getMemberId(), language);

        return ResponseEntity.ok(new RsData<>(
                "200",
                "언어 설정이 완료되었습니다."
        ));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 정보 확인 성공", content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @PossibleErrors(MEMBER_NOT_FOUND)
    @GetMapping("/me")
    public ResponseEntity<RsData<UserProfileResponse>> getMyProfile(
            @Parameter(hidden = true) @Login CustomUserDetails userDetails,
            HttpServletResponse response
    ) {

        Long memberId = userDetails.getMemberId();
        String platformId = memberService.findIdForPlatformId(userDetails.getMemberId());
        validateWithdrawnLogNotRejoinable(platformId, response, memberId);

        UserProfileResponse userProfile = memberService.getUserProfile(memberId);

        redisTemplate.opsForSet().add("online-users", String.valueOf(memberId));

        return ResponseEntity.ok(new RsData<>(
                "200",
                "내 정보 확인 성공", userProfile
        ));
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 사용 가능한지 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 사용 가능 여부 반환"),
            @ApiResponse(responseCode = "400", description = "파라미터 검증 오류 메시지")
    })
    @PostMapping("/check-nickname")
    public ResponseEntity<RsData<Boolean>> checkNickname(
            @RequestParam("nickname")
            @NotBlank(message = "닉네임을 입력해 주세요.")
            @Size(min = 1, max = 15, message = "닉네임은 1자 이상 15자 이하로 입력해 주세요.")
            String nickname
    ) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);

        return ResponseEntity.ok(new RsData<>(
                "200",
                isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                isAvailable
        ));
    }

    /**
     * @param request     변경할 닉네임/언어 설정 정보가 담긴 요청 DTO
     * @param userDetails 인증된 회원의 사용자 정보
     * @return 회원 정보 변경 결과를 포함한 표준 응답 객체
     */
    @Operation(summary = "회원 정보 변경", description = "회원의 닉네임 / 언어 설정을 변경할 수 있습니다.")
    @PossibleErrors({DUPLICATE_FILED, MEMBER_NOT_FOUND})
    @PatchMapping("/me")
    public ResponseEntity<RsData<ChangeInfoResponse>> changeMemberInformation(
            @RequestBody ChangeInfoRequest request,
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        return ResponseEntity.ok(new RsData<>(
                "200",
                "회원 정보가 수정되었습니다.",
                memberService.changeInformation(memberId, request)
        ));
    }

    /**
     * @param file        새 프로필 이미지 파일 (필수)
     * @param userDetails 인증된 회원 정보
     * @return 변경된 프로필 이미지의 S3 URL을 포함한 응답
     */
    @Operation(summary = "프로필 이미지 변경", description = "인증된 회원의 프로필 이미지를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지가 수정되었습니다.", content = @Content(schema = @Schema(type = "string", format = "uri", example = "https:/s3_버킷_주소/profile.jpg", description = "수정된 프로필 이미지의 URL")))
    @PossibleErrors({MEMBER_NOT_FOUND, FILE_EMPTY, NOT_SUPPORTED_TYPE, NOT_EXIST_BUCKET})
    @PatchMapping("/me/profile")
    public ResponseEntity<RsData<String>> changeProfileImage(
            @RequestParam("file")
            @Valid @NotNull MultipartFile file,
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        return ResponseEntity.ok(new RsData<>(
                "200",
                "프로필 이미지가 수정되었습니다.",
                memberService.changeProfile(memberId, file)
        ));
    }

    @Operation(summary = "회원 로그아웃", description = "현재 로그인한 사용자의 세션 및 토큰을 만료시켜 로그아웃 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PossibleErrors(TOKEN_NOT_FOUND)
    @GetMapping("/logout")
    public ResponseEntity<RsData<Void>> logout(
            HttpServletResponse response,
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        expiredCookies(response, userDetails.getMemberId());

        Sentry.configureScope(scope -> scope.setUser(null));
        redisTemplate.opsForSet().remove("online-users", String.valueOf(memberId));

        return ResponseEntity.ok(new RsData<>(
                "200",
                "로그아웃에 성공하셨습니다."
        ));
    }

    /**
     * 회원의 정보는 마스킹 처리 및 논리 삭제, 6개월 후 자동으로 DB에서 완전히 삭제됩니다.
     */
    @Operation(summary = "회원 탈퇴", description = "인증된 회원의 탈퇴 요청을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴가 완료되었습니다.")
    @PossibleErrors({MEMBER_ALREADY_WITHDRAWN, MEMBER_NOT_FOUND, NOT_EXIST_BUCKET})
    @DeleteMapping("/me")
    public ResponseEntity<RsData<Void>> delete(
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        Long memberId = userDetails.getMemberId();

        memberService.withdrawMember(memberId);
        memberService.deleteOldProfileImage(memberId);

        expiredCookies(response, userDetails.getMemberId());

        return ResponseEntity.ok(new RsData<>(
                "200",
                "회원 탈퇴가 완료되었습니다."
        ));
    }

    /**
     * 탈퇴 이력이 있는 회원의 재가입 가능 여부를 검증합니다.
     * 재가입 가능일 이전일 시 회원 정보를 삭제하고 예외를 발생시킵니다.
     *
     * @param platformId 플랫폼 식별자
     * @param response   HTTP 응답 객체
     * @param memberId   회원 PK
     */
    private void validateWithdrawnLogNotRejoinable(String platformId, HttpServletResponse response, Long memberId) {
        // 탈퇴 이력이 없으면 바로 리턴
        if (!withdrawalService.existsByOriginalPlatformId(platformId)) {
            return;
        }

        // 탈퇴 이력 조회
        Optional.ofNullable(withdrawalService.findByOriginalPlatformId(platformId))
                .map(WithdrawnLog::getCreatedAt)
                .map(dt -> dt.plusDays(30))
                .filter(date -> LocalDateTime.now().isBefore(date))
                // 재가입 가능일이 아직 지나지 않은 경우 예외 처리
                .ifPresent(date -> {
                    expiredCookies(response, memberId);
                    memberService.deleteMember(memberId);
                    throw new ServiceException(CANNOT_SIGNUP_WITH_THIS_ID);
                });

    }

    private void expiredCookies(HttpServletResponse response, Long memberId) {
        tokenService.deleteTokenInCookie(response, ACCESS_TOKEN);
        tokenService.invalidateTokenAndDeleteRedisRefreshToken(response, memberId);
    }
}

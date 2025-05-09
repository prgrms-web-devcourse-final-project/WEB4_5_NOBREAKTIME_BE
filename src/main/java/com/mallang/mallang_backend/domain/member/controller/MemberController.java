package com.mallang.mallang_backend.domain.member.controller;

import static com.mallang.mallang_backend.global.constants.AppConstants.ACCESS_TOKEN;
import static com.mallang.mallang_backend.global.constants.AppConstants.REFRESH_TOKEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mallang.mallang_backend.domain.member.dto.ChangeInfoRequest;
import com.mallang.mallang_backend.domain.member.dto.ChangeInfoResponse;
import com.mallang.mallang_backend.domain.member.dto.UserProfileResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import com.mallang.mallang_backend.global.token.JwtService;
import com.mallang.mallang_backend.global.token.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Tag(name = "Member", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenService tokenService;
    private final JwtService jwtService;

    /**
     * @param userDetails 로그인 사용자 정보
     * @param language    변경할 언어
     */
    @Operation(
            summary = "사용자 학습 언어 변경",
            description = "로그인한 사용자의 학습 언어를 변경합니다."
    )
    @ApiResponse(responseCode = "200", description = "언어 설정이 완료되었습니다.")
    @PossibleErrors({MEMBER_NOT_FOUND, LANGUAGE_ALREADY_SET})
    @PatchMapping("/update-language")
    public ResponseEntity<RsData<?>> updateUserLanguage(
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails,
            @Parameter(description = "변경할 언어", required = true, example = "ENGLISH")
            @RequestParam("language") Language language) {

        memberService.updateLearningLanguage(userDetails.getMemberId(), language);

        RsData<Object> response = new RsData<>(
                "200",
                "언어 설정이 완료되었습니다."
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "내 정보 확인 성공",
            content = @Content(
                    schema = @Schema(implementation = UserProfileResponse.class)
            ))
    @PossibleErrors(MEMBER_NOT_FOUND)
    @GetMapping("/me")
    public ResponseEntity<RsData<?>> getMyProfile(
            @Parameter(hidden = true)
            @Login CustomUserDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        UserProfileResponse userProfile = memberService.getUserProfile(memberId);

        RsData<?> response = new RsData<>(
                "200",
                "내 정보 확인 성공",
                userProfile);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "이메일 중복 체크",
            description = "이메일이 사용 가능한지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 이메일입니다."),
            @ApiResponse(responseCode = "400", description = "파라미터 검증 오류 메시지")
    })
    @PossibleErrors({DUPLICATE_FILED, MEMBER_NOT_FOUND})
    @PostMapping("/check-email")
    public ResponseEntity<RsData<?>> checkEmail(@RequestParam("email")
                                                @NotBlank
                                                @Email(message = "유효한 이메일 형식이 아닙니다.")
                                                String email) {

        memberService.validateEmailNotDuplicated(email);

        RsData<Boolean> response = new RsData<>(
                "200",
                "사용 가능한 이메일입니다."
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "닉네임 중복 체크",
            description = "닉네임이 사용 가능한지 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 사용 가능 여부 반환"),
            @ApiResponse(responseCode = "400", description = "파라미터 검증 오류 메시지")
    })
    @PostMapping("/check-nickname")
    public ResponseEntity<RsData<Boolean>> checkNickname(@RequestParam("nickname")
                                                         @NotBlank(message = "닉네임을 입력해 주세요.")
                                                         @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하로 입력해 주세요.")
                                                         String nickname) {

        boolean isAvailable = memberService.isNicknameAvailable(nickname);

        RsData<Boolean> response = new RsData<>(
                "200",
                isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                isAvailable
        );

        return ResponseEntity.ok(response);
    }

    /**
     * @param request     변경할 닉네임/이메일 정보가 담긴 요청 DTO
     * @param userDetails 인증된 회원의 사용자 정보
     * @return 회원 정보 변경 결과를 포함한 표준 응답 객체
     */
    @Operation(
            summary = "회원 정보 변경",
            description = "회원의 닉네임 / 이메일을 변경할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원 정보가 성공적으로 수정되었습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangeInfoResponse.class)
            ))
    @PossibleErrors({DUPLICATE_FILED, MEMBER_NOT_FOUND})
    @PatchMapping("/me")
    public ResponseEntity<RsData<?>> changeMemberInformation(ChangeInfoRequest request,
                                                             @Parameter(hidden = true)
                                                             @Login CustomUserDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        ChangeInfoResponse changeResponse =
                memberService.changeInformation(memberId, request);

        RsData<?> response = new RsData<>(
                "200",
                "회원 정보가 수정되었습니다.",
                changeResponse
        );

        return ResponseEntity.ok(response);
    }

    /**
     * @param file        새 프로필 이미지 파일 (필수)
     * @param userDetails 인증된 회원 정보
     * @return 변경된 프로필 이미지의 S3 URL을 포함한 응답
     */
    @Operation(
            summary = "프로필 이미지 변경",
            description = "인증된 회원의 프로필 이미지를 변경합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "프로필 이미지가 수정되었습니다.",
            content = @Content(
                    schema = @Schema(type = "string", format = "uri", example = "https:/s3_버킷_주소/profile.jpg", description = "수정된 프로필 이미지의 URL")
            ))
    @PossibleErrors({MEMBER_NOT_FOUND, FILE_EMPTY, NOT_SUPPORTED_TYPE, NOT_EXIST_BUCKET})
    @PatchMapping("/me/profile")
    public ResponseEntity<RsData<String>> changeProfileImage(@RequestParam("file")
                                                             @Valid
                                                             @NotNull MultipartFile file,
                                                             @Parameter(hidden = true)
                                                             @Login CustomUserDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        String s3Url = memberService.changeProfile(memberId, file);

        RsData<String> response = new RsData<>(
                "200",
                "프로필 이미지가 수정되었습니다.",
                s3Url
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원 로그아웃",
            description = "현재 로그인한 사용자의 세션 및 토큰을 만료시켜 로그아웃 처리합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PossibleErrors(TOKEN_NOT_FOUND)
    @GetMapping("/logout")
    public ResponseEntity<RsData<?>> logout(HttpServletResponse response,
                                            @Parameter(hidden = true)
                                            @Login CustomUserDetails userDetails) {

        tokenService.deleteTokenInCookie(response, ACCESS_TOKEN);
        tokenService.invalidateTokenAndDeleteRedisRefreshToken(response, userDetails.getMemberId());

        RsData<Object> rsp = new RsData<>(
                "200",
                "로그아웃에 성공하셨습니다.");

        return ResponseEntity.ok(rsp);
    }

    /**
     * 회원의 정보는 마스킹 처리 및 논리 삭제, 6개월 후 자동으로 DB에서 완전히 삭제됩니다.
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "인증된 회원의 탈퇴 요청을 처리합니다."
    )
    @ApiResponse(responseCode = "200", description = "회원 탈퇴가 완료되었습니다.")
    @PossibleErrors({MEMBER_ALREADY_WITHDRAWN, MEMBER_NOT_FOUND, NOT_EXIST_BUCKET})
    @DeleteMapping("/me")
    public ResponseEntity<RsData<Void>> delete(@Parameter(hidden = true)
                                               @Login CustomUserDetails userDetails) {

        memberService.withdrawMember(userDetails.getMemberId());

        RsData<Void> response = new RsData<>(
                "200",
                "회원 탈퇴가 완료되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}

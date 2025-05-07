package com.mallang.mallang_backend.domain.member.controller;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.service.MemberService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 탈퇴 요청을 처리
     *
     * @param userDetails 인증된 회원 정보
     * @return 탈퇴 완료 응답
     */
    @Operation(summary = "회원 탈퇴", description = "인증된 회원의 탈퇴 요청을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴가 완료되었습니다.")
    @DeleteMapping
    public ResponseEntity<RsData<Void>> delete(
            @Login CustomUserDetails userDetails) {

        memberService.withdrawMember(userDetails.getMemberId());
        RsData<Void> response = new RsData<>(
                "200",
                "회원 탈퇴가 완료되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 프로필 이미지를 변경
     *
     * @param file        새 프로필 이미지 파일 (필수)
     * @param userDetails 인증된 회원 정보
     * @return 변경된 프로필 이미지의 S3 URL을 포함한 응답
     */
    @Operation(summary = "프로필 이미지 변경", description = "인증된 회원의 프로필 이미지를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지가 수정되었습니다.")
    @PatchMapping("/profile")
    public ResponseEntity<RsData<String>> changeProfileImage(
            @RequestParam("file") @Valid @NotNull MultipartFile file,
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

    // 소셜 로그인 실행 후 대시보드 페이지 리다이렉트 시 보여지는 정보
    @GetMapping("/dashboard")
    public ResponseEntity<RsData<?>> inDashboard(@AuthenticationPrincipal
                                                    OAuth2User principal) {

        String email = principal.getName();
        Member member = memberService.getMemberByEmail(email);

        return ResponseEntity.ok().body(
                new RsData<>("200",
                        "환영합니다, " + member.getNickname() + "님!",
                        member
                ));
    }
}

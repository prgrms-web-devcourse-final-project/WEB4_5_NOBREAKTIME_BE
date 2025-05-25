package com.mallang.mallang_backend.domain.member.dto;

import com.mallang.mallang_backend.global.common.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보 수정 응답 DTO")
public class ChangeInfoResponse {

    @Schema(description = "회원 닉네임", example = "test_user")
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하로 입력해 주세요.")
    private String nickname;

    @Schema(description = "회원 언어", example = "ENGLISH")
    @NotBlank(message = "언어 선택을 입력해주세요.")
    private Language language;
}

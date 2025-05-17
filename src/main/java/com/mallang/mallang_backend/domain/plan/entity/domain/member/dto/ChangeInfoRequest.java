package com.mallang.mallang_backend.domain.plan.entity.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "회원 정보 수정 요청 DTO")
public class ChangeInfoRequest {

    @Schema(description = "회원 닉네임", example = "test_user")
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하로 입력해 주세요.")
    private String nickname;

    @Schema(description = "회원 이메일", example = "user@example.com")
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}

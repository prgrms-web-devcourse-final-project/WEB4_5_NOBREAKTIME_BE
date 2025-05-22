package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionSaveRequest {
    @NotBlank(message = "영상 Id는 필수입니다.")
    private String videoId;  // 영상 아이디

    @NotNull(message = "자막 Id는 필수입니다.")
    private Long subtitleId; // 자막 아이디
}

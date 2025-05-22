package com.mallang.mallang_backend.domain.sentence.expressionbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionBookRequest {
    @NotBlank(message = "표현함 이름은 필수입니다.")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]*$", message = "특수문자는 포함할 수 없습니다.")
    private String name;
}

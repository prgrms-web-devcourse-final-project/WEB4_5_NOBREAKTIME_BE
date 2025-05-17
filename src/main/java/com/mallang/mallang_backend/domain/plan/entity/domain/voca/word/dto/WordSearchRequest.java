package com.mallang.mallang_backend.domain.plan.entity.domain.voca.word.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WordSearchRequest {
    @NotBlank(message = "단어는 공백일 수 없습니다.")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]*$", message = "특수문자는 포함할 수 없습니다.")
    private String word;
}

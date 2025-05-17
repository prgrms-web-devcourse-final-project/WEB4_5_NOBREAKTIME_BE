package com.mallang.mallang_backend.domain.plan.entity.domain.voca.wordbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordbookCreateRequest {
	@NotBlank(message = "단어장 이름은 공백일 수 없습니다.")
	@Pattern(regexp = "^[\\p{L}\\p{N} ]*$", message = "특수문자는 포함할 수 없습니다.")
	private String name;
}

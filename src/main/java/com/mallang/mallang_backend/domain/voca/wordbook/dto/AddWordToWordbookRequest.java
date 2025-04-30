package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWordToWordbookRequest {
	private String word;
	private Long subtitleId;
	private Long videoId;
}

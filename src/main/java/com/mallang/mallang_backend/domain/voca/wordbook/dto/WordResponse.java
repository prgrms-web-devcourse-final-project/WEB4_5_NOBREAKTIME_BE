package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WordResponse {
	private String word;
	private String videoId;
	private Long subtitleId;
	private LocalDateTime createdAt;
}
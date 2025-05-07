package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class WordResponse {
	private String word;
	private String pos;
	private String meaning;
	private String difficulty;
	private String exampleSentence;
	private String translatedSentence;
	private String videoId;
	private Long subtitleId;
	private LocalDateTime createdAt;
}
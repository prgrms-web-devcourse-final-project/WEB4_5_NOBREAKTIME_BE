package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WordMoveRequest {
	@NotNull
	private Long destinationWordbookId;
	private List<WordMoveItem> words;
}

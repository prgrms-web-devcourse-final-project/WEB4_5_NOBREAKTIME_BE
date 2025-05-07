package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddWordToWordbookListRequest {
	private List<AddWordToWordbookRequest> words;
}

package com.mallang.mallang_backend.domain.voca.wordbook.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWordToWordbookListRequest {
	private List<AddWordToWordbookRequest> words;
}

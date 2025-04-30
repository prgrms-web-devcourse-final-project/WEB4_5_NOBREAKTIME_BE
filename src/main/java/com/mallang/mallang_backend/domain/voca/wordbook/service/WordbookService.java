package com.mallang.mallang_backend.domain.voca.wordbook.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;

public interface WordbookService {
	void addWords(Long wordbookId, AddWordToWordbookListRequest request, Member member);
}

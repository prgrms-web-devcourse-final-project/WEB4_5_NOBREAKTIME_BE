package com.mallang.mallang_backend.domain.voca.wordbook.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;

public interface WordbookService {
	void addWords(Long wordbookId, AddWordToWordbookListRequest request, Member member);
	void addWordCustom(Long wordbookId, AddWordRequest request, Member member);
	Long createWordbook(WordbookCreateRequest request, Member member);
	void renameWordbook(Long wordbookId, String name, Member member);
	void deleteWordbook(Long wordbookId, Member member);
}

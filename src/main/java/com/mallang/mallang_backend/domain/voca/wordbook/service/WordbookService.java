package com.mallang.mallang_backend.domain.voca.wordbook.service;

import java.util.List;

import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookResponse;

public interface WordbookService {
	void addWords(Long wordbookId, AddWordToWordbookListRequest request, Long memberId);

	void addWordCustom(Long wordbookId, AddWordRequest request, Long memberId);

	Long createWordbook(WordbookCreateRequest request, Long memberId);

	void renameWordbook(Long wordbookId, String name, Long memberId);

	void deleteWordbook(Long wordbookId, Long memberId);

	void moveWords(WordMoveRequest request, Long memberId);

	void deleteWords(WordDeleteRequest request, Long memberId);

	List<WordResponse> getWordsRandomly(Long wordbookId, Long memberId);

	List<WordbookResponse> getWordbooks(Long memberId);
	
	List<WordResponse> searchWordFromWordbook(Long memberId, String keyword);

	List<WordResponse> getWordbookItems(Long wordbookId, Long memberId);
}

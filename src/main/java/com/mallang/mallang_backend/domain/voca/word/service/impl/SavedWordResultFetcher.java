package com.mallang.mallang_backend.domain.voca.word.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavedWordResultFetcher {

	private final WordRepository wordRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public List<Word> fetchSavedWordResultAfterWait(String word) {
		return wordRepository.findByWord(word); // DB 조회
	}
}

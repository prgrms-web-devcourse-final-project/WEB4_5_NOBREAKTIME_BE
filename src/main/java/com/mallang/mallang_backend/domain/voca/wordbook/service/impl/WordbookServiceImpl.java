package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordbookServiceImpl implements WordbookService {

	private final WordbookRepository wordbookRepository;
	private final WordRepository wordRepository;
	private final WordbookItemRepository wordbookItemRepository;

	@Transactional
	@Override
	public void addWords(Long wordbookId, AddWordToWordbookListRequest request, Member member) {
		// 단어장 존재 + 권한 체크
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new IllegalArgumentException("해당 단어장이 존재하지 않거나 권한이 없습니다."));

		for (AddWordToWordbookRequest dto : request.getWords()) {
			// 단어가 이미 있는지 확인하고 없으면 저장
			List<Word> words = wordRepository.findByWord(dto.getWord());

			if (words.isEmpty()) {
				// TODO: 저장된 단어가 없는 경우, 사전 API 또는 GPT 처리해서 word 추가 (일반적인 경우엔 단어가 이미 존재함)
			}

			// 단어가 단어장에 저장되어 있지 않을 때만 저장
			if (wordbookItemRepository.findByWordbookIdAndWord(wordbook.getId(), dto.getWord()).isEmpty()) {

				// WordbookItem 생성 및 저장
				WordbookItem item = WordbookItem.builder()
					.wordbook(wordbook)
					.word(dto.getWord())
					.subtitleId(dto.getSubtitleId())
					.videoId(dto.getVideoId())
					.build();

				wordbookItemRepository.save(item);
			}
		}
	}

	// 커스텀 단어 추가
	@Transactional
	@Override
	public void addWordCustom(Long wordbookId, AddWordRequest request, Member member) {
		// 단어장 존재 + 권한 체크
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		String word = request.getWord();
		// 단어가 이미 있는지 확인하고 없으면 저장
		List<Word> words = wordRepository.findByWord(word);

		if (words.isEmpty()) {
			// TODO: 저장된 단어가 없는 경우, 사전 API 또는 GPT 처리해서 word 추가 (일반적인 경우엔 단어가 이미 존재함)
		}

		// 단어가 단어장에 저장되어 있지 않을 때만 저장
		if (wordbookItemRepository.findByWordbookIdAndWord(wordbook.getId(), word).isEmpty()) {

			// WordbookItem 생성 및 저장
			WordbookItem item = WordbookItem.builder()
				.wordbook(wordbook)
				.word(word)
				.subtitleId(null)
				.videoId(null)
				.build();

			wordbookItemRepository.save(item);
		}
	}

	// 단어장 생성
	@Transactional
	@Override
	public Long createWordbook(WordbookCreateRequest request, Member member) {
		if (!member.canCreateWordBook()) {
			throw new ServiceException(NO_WORDBOOK_CREATE_PERMISSION);
		}

		if (request.getName().equals(DEFAULT_WORDBOOK_NAME)) {
			throw new ServiceException(WORDBOOK_CREATE_DEFAULT_FORBIDDEN);
		}

		Wordbook wordbook = Wordbook.builder()
			.member(member)
			.name(request.getName())
			.language(member.getLanguage())
			.build();

		return wordbookRepository.save(wordbook).getId();
	}

	// 단어장 이름 변경
	@Transactional
	@Override
	public void renameWordbook(Long wordbookId, String name, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		wordbook.updateName(name);
	}
}

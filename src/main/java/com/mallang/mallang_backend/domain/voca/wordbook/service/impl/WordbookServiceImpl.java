package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteItem;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveItem;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookResponse;
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

	// 단어장에 단어 추가
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

	// 단어장에 커스텀 단어 추가
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

	// 추가 단어장 생성
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

	// 추가 단어장 이름 변경
	@Transactional
	@Override
	public void renameWordbook(Long wordbookId, String name, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		wordbook.updateName(name);
	}

	// 추가 단어장 삭제
	@Transactional
	@Override
	public void deleteWordbook(Long wordbookId, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		if (DEFAULT_WORDBOOK_NAME.equals(wordbook.getName())) {
			throw new ServiceException(WORDBOOK_DELETE_DEFAULT_FORBIDDEN);
		}
		// 추가 단어장 삭제 시 들어있는 단어 아이템들도 삭제
		wordbookItemRepository.deleteAllByWordbookId(wordbookId);
		wordbookRepository.delete(wordbook);
	}

	// 단어장의 단어 이동
	@Transactional
	@Override
	public void moveWords(WordMoveRequest request, Member member) {
		Long toId = request.getDestinationWordbookId();

		Wordbook toWordbook = wordbookRepository.findByIdAndMember(toId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		for (WordMoveItem item : request.getWords()) {
			// 같은 단어장으로 이동하려고 하면 통과
			if (toId.equals(item.getFromWordbookId())) {
				continue;
			}

			// 기존 단어장 조회
			Wordbook fromWordbook = wordbookRepository.findByIdAndMember(item.getFromWordbookId(), member)
				.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

			// 기존 WordbookItem 찾기
			WordbookItem existingItem = wordbookItemRepository.findByWordbookAndWord(fromWordbook, item.getWord())
				.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

			// 기존 데이터 삭제
			wordbookItemRepository.delete(existingItem);

			// 새 WordbookItem 생성 후 저장 (id 새로 생성됨)
			WordbookItem movedItem = WordbookItem.builder()
				.wordbook(toWordbook)
				.word(existingItem.getWord())
				.subtitleId(existingItem.getSubtitleId())
				.videoId(existingItem.getVideoId())
				.build();

			wordbookItemRepository.save(movedItem);
		}
	}

	// 단어장에서 단어 삭제
	@Transactional
	@Override
	public void deleteWords(WordDeleteRequest request, Member member) {
		for (WordDeleteItem item : request.getWords()) {
			// 단어장 조회 및 권한 체크
			Wordbook wordbook = wordbookRepository.findByIdAndMember(item.getWordbookId(), member)
				.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

			// WordbookItem 조회
			WordbookItem itemToDelete = wordbookItemRepository.findByWordbookAndWord(wordbook, item.getWord())
				.orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

			wordbookItemRepository.delete(itemToDelete);
		}
	}

	// 단어장에 단어들 조회(랜덤 순서)
	@Transactional(readOnly = true)
	@Override
	public List<WordResponse> getWordsRandomly(Long wordbookId, Member member) {
		// 단어장 존재 여부 및 권한 확인
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		// 단어장 아이템 조회
		List<WordbookItem> items = wordbookItemRepository.findAllByWordbook(wordbook);

		// 무작위 순서로 섞기
		Collections.shuffle(items);

		return items.stream()
			.map(item -> new WordResponse(
					item.getWord(),
					item.getVideoId(),
					item.getSubtitleId(),
					item.getCreatedAt()
				)
			).collect(Collectors.toList());
	}

	// 단어장 조회
	@Transactional(readOnly = true)
	@Override
	public List<WordbookResponse> getWordbooks(Member member) {
		List<Wordbook> wordbooks = wordbookRepository.findAllByMember(member);

		return wordbooks.stream()
			.map(w -> new WordbookResponse(
				w.getId(),
				w.getName(),
				w.getLanguage()
			)).toList();
	}
}

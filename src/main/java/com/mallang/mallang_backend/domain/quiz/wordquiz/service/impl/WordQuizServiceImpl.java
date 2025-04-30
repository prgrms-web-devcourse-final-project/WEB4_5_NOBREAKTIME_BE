package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.NO_WORDBOOK_EXIST_OR_FORBIDDEN;
import static com.mallang.mallang_backend.global.exception.ErrorCode.WORDBOOK_IS_EMPTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItemDto;
import com.mallang.mallang_backend.domain.quiz.wordquiz.service.WordQuizService;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WordQuizServiceImpl implements WordQuizService {

	private final WordbookRepository wordbookRepository;
	private final WordbookItemRepository wordbookItemRepository;
	private final SubtitleRepository subtitleRepository;
	private final WordRepository wordRepository;

	@Override
	public List<WordQuizItemDto> generateWordbookQuiz(Long wordbookId, Member member) {
		Wordbook wordbook = wordbookRepository.findByIdAndMember(wordbookId, member)
			.orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

		List<WordbookItem> items = wordbookItemRepository.findAllByWordbook(wordbook);
		if (items.isEmpty()) {
			throw new ServiceException(WORDBOOK_IS_EMPTY);
		}

		List<WordQuizItemDto> quizzes = items.stream()
			.map(this::convertToQuizDto)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(ArrayList::new));

		Collections.shuffle(quizzes);
		return quizzes;
	}

	private WordQuizItemDto convertToQuizDto(WordbookItem item) {
		if (item.getSubtitleId() == null) {
			return createQuizFromCustomWord(item);
		} else {
			return createQuizFromSubtitle(item);
		}
	}

	private WordQuizItemDto createQuizFromCustomWord(WordbookItem item) {
		return wordRepository.findByWord(item.getWord())
			.stream()
			.findAny()
			.map(word -> createDto(item.getId(), item.getWord(), word.getExampleSentence(), word.getTranslatedSentence()))
			.orElse(null);
	}

	private WordQuizItemDto createQuizFromSubtitle(WordbookItem item) {
		return subtitleRepository.findById(item.getSubtitleId())
			.map(sub -> createDto(item.getId(), item.getWord(), sub.getOriginalSentence(), sub.getTranslatedSentence()))
			.orElse(null);
	}

	private WordQuizItemDto createDto(Long id, String word, String original, String translated) {
		WordQuizItemDto dto = new WordQuizItemDto();
		dto.setId(id);
		dto.setWord(word);
		dto.setOriginal(original);
		dto.setTranslated(translated);
		return dto;
	}
}

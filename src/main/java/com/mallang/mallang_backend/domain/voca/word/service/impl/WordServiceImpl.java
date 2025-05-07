package com.mallang.mallang_backend.domain.voca.word.service.impl;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final GptService gptService;

    @Override
    public WordSearchResponse savedWord(String word) {
        List<Word> words = wordRepository.findByWord(word); // DB 조회
        if (!words.isEmpty()) {
            return new WordSearchResponse(convertToResponse(words));    // DB에 존재하면 변환하여 반환
        }

        String gptResult = gptService.searchWord(word); // DB에 없으면 GPT 호출
        List<Word> generatedWords = parseGptResult(word, gptResult); // GPT 결과 파싱
        try {
            // 저장
            wordRepository.saveAll(generatedWords);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.WORD_SAVE_FAILED, e);
        }

        return new WordSearchResponse(convertToResponse(generatedWords));   // 변환 후 반환
    }

    @Override
    public WordSearchResponse searchWord(String word) {
        List<Word> words = wordRepository.findByWord(word);
        if (words.isEmpty()) {
            throw new ServiceException(ErrorCode.WORD_NOT_FOUND);
        }

        return new WordSearchResponse(convertToResponse(words));
    }

    /**
     * Word 엔티티 리스트를 WordMeaning 응답 객체 리스트로 변환합니다.
     *
     * @param words 변환할 Word 리스트
     * @return 변환된 WordMeaning 리스트
     */
    private List<WordSearchResponse.WordMeaning> convertToResponse(List<Word> words) {
        return words.stream()
                .map(WordSearchResponse.WordMeaning::fromEntity)
                .collect(Collectors.toList());
    }
}
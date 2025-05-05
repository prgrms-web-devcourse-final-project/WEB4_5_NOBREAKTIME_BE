package com.mallang.mallang_backend.domain.voca.word.service.impl;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSavedResponse;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final GptService gptService;

    @Override
    public WordSavedResponse savedWord(String word) {
        List<Word> words = wordRepository.findByWord(word); // DB 조회
        if (!words.isEmpty()) {
            return new WordSavedResponse(convertToResponse(words));    // DB에 존재하면 변환하여 반환
        }

        String gptResult = gptService.searchWord(word); // DB에 없으면 GPT 호출
        List<Word> generatedWords = parseGptResult(word, gptResult); // GPT 결과 파싱
        try {
            // 저장
            wordRepository.saveAll(generatedWords);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.WORD_SAVE_FAILED, e);
        }

        return new WordSavedResponse(convertToResponse(generatedWords));   // 변환 후 반환
    }

    /**
     * Word 엔티티 리스트를 WordMeaning 응답 객체 리스트로 변환합니다.
     *
     * @param words 변환할 Word 리스트
     * @return 변환된 WordMeaning 리스트
     */
    private List<WordSavedResponse.WordMeaning> convertToResponse(List<Word> words) {
        return words.stream()
                .map(WordSavedResponse.WordMeaning::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * GPT로부터 받은 문자열 결과를 파싱하여 Word 엔티티 리스트로 변환합니다.
     *
     * @param word      검색한 단어
     * @param gptResult GPT 응답 결과 문자열 (품사 | 뜻 | 난이도 형식)
     * @return 파싱된 Word 엔티티 리스트
     */
    private List<Word> parseGptResult(String word, String gptResult) {
        List<Word> words = new ArrayList<>();
        String[] lines = gptResult.split("\\R"); // 결과를 줄 단위로 분리

        for (String line : lines) {
            if (line.isBlank()) continue;   // 빈 줄은 무시

            String[] parts = line.split("\\|"); // 한 줄을 '|' 기준으로 나누기
            if (parts.length != 5) {
                // 품사|뜻|난이도 형식이 아닌 경우
                throw new ServiceException(ErrorCode.WORD_PARSE_FAILED);
            }

            try {
                // 각 부분(품사, 뜻, 난이도)을 trim 처리
                String pos = parts[0].trim();      // 품사
                String meaning = parts[1].trim();  // 뜻
                int difficultyValue = Integer.parseInt(parts[2].trim()); // 난이도
                String exampleSentence = parts[3].trim();
                String translatedSentence =  parts[4].trim();

                Word newWord = Word.builder()
                        .word(word)
                        .pos(pos)
                        .meaning(meaning)
                        .difficulty(Difficulty.fromValue(difficultyValue))
                        .exampleSentence(exampleSentence)
                        .translatedSentence(translatedSentence)
                        .build();
                words.add(newWord);
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCode.GPT_RESPONSE_PARSE_FAIL, e);
            }
        }

        return words;
    }
}
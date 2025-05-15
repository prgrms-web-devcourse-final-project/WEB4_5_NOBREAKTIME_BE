package com.mallang.mallang_backend.global.gpt.service;

import java.util.List;

import com.mallang.mallang_backend.domain.dashboard.dto.LevelCheckResponse;
import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;

public interface GptService {

    /**
     * 단어를 검색하여 GPT 응답을 반환합니다.
     *
     * @param word 검색할 단어
     * @return GPT 응답 결과
     */
    List<Word> searchWord(String word);

    /**
     * 문장을 분석하여 GPT 응답을 반환합니다.
     *
     * @param sentence 검색할 문장(원문)
     * @param translatedSentence 원문 번역 문장
     * @return GPT 응답 결과(원문 분석)
     */
    String analyzeSentence(String sentence, String translatedSentence);

    /**
     * 자막 세그먼트 리스트를 GPT에 전달하여 분석 결과를 반환합니다.
     *
     * @param segments 분석할 자막 세그먼트 리스트 (startTime, speaker, 원문 문장 포함)
     * @return 번역 및 키워드가 포함된 GPT 분석 결과 리스트
     */
    List<GptSubtitleResponse> analyzeScript(List<TranscriptSegment> segments);

    LevelCheckResponse checkLevel(String wordLevel, String expressionLevel, String wordQuizResultString, String expressionResultString);
}
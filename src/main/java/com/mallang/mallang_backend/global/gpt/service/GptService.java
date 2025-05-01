package com.mallang.mallang_backend.global.gpt.service;

public interface GptService {

    /**
     * 단어를 검색하여 GPT 응답을 반환합니다.
     *
     * @param word 검색할 단어
     * @return GPT 응답 결과
     */
    String searchWord(String word);

    /**
     * 문장을 분석하여 GPT 응답을 반환합니다.
     *
     * @param sentence 검색할 문장(원문)
     * @param translatedSentence 원문 번역 문장
     * @return GPT 응답 결과(원문 분석)
     */
    String analyzeSentence(String sentence, String translatedSentence);
}
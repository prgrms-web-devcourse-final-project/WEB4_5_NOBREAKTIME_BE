package com.mallang.mallang_backend.domain.voca.word.service;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;

public interface WordService {

    /**
     * 단어를 검색하여 품사/해석/난이도 목록을 저장합니다.
     *
     * @param word 저장할 단어
     * @return WordSavedResponse
     */
    WordSearchResponse savedWord(String word);

    /**
     * 단어를 검색하여 품사/해석/난이도 목록을 반환합니다.
     * DB에 없으면 null값을 반환하고 비동기로 단어를 GPT 검색하여 저장합니다.
     *
     * @param word 검색할 단어
     * @return WordSavedResponse
     */
    WordSearchResponse searchWord(String word);
}

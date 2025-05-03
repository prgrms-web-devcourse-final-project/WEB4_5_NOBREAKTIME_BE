package com.mallang.mallang_backend.domain.voca.word.service;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;

public interface WordService {

    /**
     * 단어를 검색하여 품사/해석/난이도 목록을 반환합니다.
     * DB에 없으면 GPT를 통해 가져옵니다.
     *
     * @param word 검색할 단어
     * @return WordSearchResponse
     */
    WordSearchResponse searchWord(String word);
}

package com.mallang.mallang_backend.domain.voca.word.controller;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchRequest;
import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    /**
     * 단어를 입력받아 품사/해석/난이도를 분석하여 반환하는 API
     *
     * @param wordSearchRequest WordRequest - 단어
     * @return WordSearchResponse - 품사/해석/난이도 리스트
     */
    @PostMapping("/search")
    public WordSearchResponse searchWord(@RequestBody WordSearchRequest wordSearchRequest) {
        return wordService.searchWord(wordSearchRequest.getWord());
    }

}

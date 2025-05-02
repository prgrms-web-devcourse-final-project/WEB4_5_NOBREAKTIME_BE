package com.mallang.mallang_backend.domain.voca.word.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchRequest;
import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/words")
@RequiredArgsConstructor
@Tag(name = "Word", description = "단어 조회 및 분석 API")
public class WordController {

    private final WordService wordService;

    /**
     * 단어를 입력받아 품사/해석/난이도를 분석하여 반환하는 API
     *
     * @param wordSearchRequest WordRequest - 단어
     * @return WordSearchResponse - 품사/해석/난이도 리스트
     */
    @PostMapping("/search")
    @Operation(
        summary = "단어 분석 조회",
        description = "입력된 단어에 대해 품사, 해석, 난이도를 분석하여 결과를 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "단어 분석 조회 성공")
        }
    )
    public ResponseEntity<RsData<WordSearchResponse>> searchWord(@RequestBody WordSearchRequest wordSearchRequest) {
        WordSearchResponse response = wordService.searchWord(wordSearchRequest.getWord());

        return ResponseEntity.ok(new RsData<>(
                "200",
                wordSearchRequest.getWord() + "에 대한 조회 결과입니다.",
                response
        ));
    }
}

package com.mallang.mallang_backend.domain.voca.word.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchRequest;
import com.mallang.mallang_backend.domain.voca.word.dto.WordSearchResponse;
import com.mallang.mallang_backend.domain.voca.word.service.WordService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/words")
@RequiredArgsConstructor
@Tag(name = "Word", description = "단어 검색 관련 API")
public class WordController {

    private final WordService wordService;

    /**
     * 단어를 입력받아 품사/해석/난이도를 분석하여 반환하는 API
     *
     * @param wordSearchRequest 단어 검색 요청 객체
     * @return 품사, 해석, 난이도 정보 리스트
     */
    @Operation(summary = "단어 저장", description = "주어진 단어에 대한 품사, 해석, 난이도 정보를 조회 후 저장합니다.")
    @ApiResponse(responseCode = "200", description = "단어 저장 결과를 반환합니다.")
    @PossibleErrors({WORD_SAVE_FAILED})
    @PostMapping("/save")
    public ResponseEntity<RsData<WordSearchResponse>> savedWord(
        @RequestBody WordSearchRequest wordSearchRequest
    ) {
        WordSearchResponse response = wordService.savedWord(wordSearchRequest.getWord());
        return ResponseEntity.ok(new RsData<>(
            "200",
            wordSearchRequest.getWord() + "에 대한 저장 결과입니다.",
            response
        ));
    }
    /**
     * 단어를 입력받아 DB조회 후 결과값을 반환하는 API
     *
     * @param wordSearchRequest 단어 검색 요청 객체
     * @return 검색한 단어에 대한 품사, 해석, 난이도 정보 리스트
     */
    @Operation(summary = "단어 검색", description = "주어진 단어에 대한 품사, 해석, 난이도 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "단어 검색 결과를 반환합니다.")
    @PossibleErrors({WORD_NOT_FOUND})
    @GetMapping("/search")
    public ResponseEntity<RsData<WordSearchResponse>> searchWord(
        WordSearchRequest wordSearchRequest
    ) {
        String word = wordSearchRequest.getWord();
        WordSearchResponse response = wordService.searchWord(wordSearchRequest.getWord());
        return ResponseEntity.ok(new RsData<>(
            "200",
            wordSearchRequest.getWord() + "에 대한 조회 결과입니다.",
            response
        ));
    }
}
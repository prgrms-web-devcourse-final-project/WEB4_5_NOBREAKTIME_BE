package com.mallang.mallang_backend.domain.sentence.expression.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.sentence.expression.service.ExpressionService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Expression", description = "표현 검색 관련 API")
@RestController
@RequestMapping("/api/v1/expressions")
@RequiredArgsConstructor
public class ExpressionController {

    private final ExpressionService expressionService;

    /**
     * 표현 검색
     *
     * @param keyword 검색어
     * @return 표현 목록
     */
    @Operation(summary = "표현 검색", description = "키워드로 표현을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "표현 검색 결과입니다.")
    @GetMapping("/search")
    public ResponseEntity<RsData<List<ExpressionResponse>>> searchExpressions(
        @RequestParam String keyword
    ) {
        List<ExpressionResponse> result = expressionService.searchExpressions(keyword);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "표현 검색 결과입니다.",
            result
        ));
    }
}

package com.mallang.mallang_backend.domain.sentence.expression.controller;

import com.mallang.mallang_backend.domain.sentence.expression.service.ExpressionService;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expressions")
@RequiredArgsConstructor
public class ExpressionController {

    private final ExpressionService expressionService;

    /**
     * 표현 검색
     * @param keyword 검색어
     * @return 표현 목록
     */
    @GetMapping("/search")
    public ResponseEntity<RsData<List<ExpressionResponse>>> searchExpressions(
            @RequestParam String keyword
    ) {
        List<ExpressionResponse> result = expressionService.searchExpressions(keyword);
        return ResponseEntity.ok(new RsData<>(
                "",
                "표현 검색 결과입니다.",
                result
        ));
    }
}
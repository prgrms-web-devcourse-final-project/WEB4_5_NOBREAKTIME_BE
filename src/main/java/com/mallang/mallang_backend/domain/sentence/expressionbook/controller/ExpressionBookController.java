package com.mallang.mallang_backend.domain.sentence.expressionbook.controller;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expressionbooks")
@RequiredArgsConstructor
public class ExpressionBookController {

    private final ExpressionBookService expressionBookService;

    /**
     * 단어를 입력받아 품사/해석/난이도를 분석하여 반환하는 API
     *
     * @param request - 표현/표현 해석
     * @return {표현}이 저장되었습니다.
     */
    @PostMapping("/{expressionbookId}/expressions")
    public ResponseEntity<RsData> savedExpression (
            @PathVariable("expressionbookId") Long expressionbookId,
            @RequestBody savedExpressionsRequest request
    ) {
        expressionBookService.save(request, expressionbookId);
        return ResponseEntity.ok(new RsData<>(
                "200",
                request.getSentence() + "이 저장되었습니다."
        ));
    }
}

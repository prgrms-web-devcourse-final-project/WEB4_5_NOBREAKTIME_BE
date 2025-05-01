package com.mallang.mallang_backend.domain.sentence.expressionbookitem.controller;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.ExpressionBookItemService;
import com.mallang.mallang_backend.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expressionbookItems")
@RequiredArgsConstructor
public class ExpressionBookItemController {
    private final ExpressionBookItemService expressionBookItemService;

    /**
     * 표현함에서 표현 삭제
     * @param request 삭제할 표현 ID 리스트와 표현함 ID, 회원 ID를 포함한 요청 객체
     * @return 성공 여부 및 메시지
     */
    @PostMapping("/expressions/delete")
    public ResponseEntity<RsData<Void>> deleteExpressionsFromBook(
            @RequestBody DeleteExpressionsRequest request
    ) {
        expressionBookItemService.deleteExpressionsFromBook(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "",
                        "표현이 표현함에서 삭제되었습니다."
                ));
    }

    /**
     * 표현함에서 표현 이동
     * @param request 이동할 표현 ID 리스트와 이동할 표현함 ID, 회원 ID를 포함한 요청 객체
     * @return  성공 여부 및 메시지
     */
    @PatchMapping("/expressions/move")
    public ResponseEntity<RsData<Void>> moveExpressionsBetweenBooks(
            @RequestBody MoveExpressionsRequest request
    ) {
        expressionBookItemService.moveExpressions(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "",
                        "표현이 다른 표현함으로 이동되었습니다."
                ));
    }
}

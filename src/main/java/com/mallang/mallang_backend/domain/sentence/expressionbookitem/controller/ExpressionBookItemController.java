package com.mallang.mallang_backend.domain.sentence.expressionbookitem.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.ExpressionBookItemService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expressionbookItems")
@RequiredArgsConstructor
@Tag(name = "ExpressionBookItem", description = "표현함 아이템 관련 API")
public class ExpressionBookItemController {
    private final ExpressionBookItemService expressionBookItemService;

    /**
     * 표현함에서 표현 삭제
     *
     * @param request     삭제할 표현 ID 리스트와 표현함 ID, 회원 ID를 포함한 요청 객체
     * @param userDetails 로그인한 사용자 정보
     * @return 성공 여부 및 메시지
     */
    @Operation(summary = "표현 삭제", description = "특정 표현함에서 표현을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "표현이 표현함에서 삭제되었습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, FORBIDDEN_EXPRESSION_BOOK})
    @PostMapping("/expressions/delete")
    public ResponseEntity<RsData<Void>> deleteExpressionsFromBook(
        @RequestBody DeleteExpressionsRequest request,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookItemService.deleteExpressionsFromBook(request, memberId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현이 표현함에서 삭제되었습니다."
            ));
    }

    /**
     * 표현함에서 표현 이동
     *
     * @param request     이동할 표현 ID 리스트와 이동할 표현함 ID, 회원 ID를 포함한 요청 객체
     * @param userDetails 로그인한 사용자 정보
     * @return 성공 여부 및 메시지
     */
    @Operation(summary = "표현 이동", description = "특정 표현함에서 다른 표현함으로 표현을 이동합니다.")
    @ApiResponse(responseCode = "200", description = "표현이 다른 표현함으로 이동되었습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, FORBIDDEN_EXPRESSION_BOOK})
    @PatchMapping("/expressions/move")
    public ResponseEntity<RsData<Void>> moveExpressionsBetweenBooks(
        @RequestBody MoveExpressionsRequest request,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookItemService.moveExpressions(request, memberId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현이 다른 표현함으로 이동되었습니다."
            ));
    }
}

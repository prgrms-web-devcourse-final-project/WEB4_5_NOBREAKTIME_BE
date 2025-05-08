package com.mallang.mallang_backend.domain.sentence.expressionbook.controller;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.UpdateExpressionBookNameRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionSaveRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "ExpressionBook", description = "추가 표현함 관련 API")
@RestController
@RequestMapping("/api/v1/expressionbooks")
@RequiredArgsConstructor
public class ExpressionBookController {

    private final ExpressionBookService expressionBookService;

    /**
     * 추가 표현함 생성
     *
     * @param request     표현함 이름과 언어 정보가 담긴 요청 객체
     * @param userDetails 로그인한 사용자 정보
     * @return 생성된 표현함 정보를 담은 응답 객체
     */
    @Operation(summary = "추가 표현함 생성", description = "추가 표현함 생성 요청을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "추가 표현함이 생성되었습니다.")
    @PossibleErrors({MEMBER_NOT_FOUND, NO_EXPRESSIONBOOK_CREATE_PERMISSION, EXPRESSIONBOOK_CREATE_DEFAULT_FORBIDDEN, DUPLICATE_EXPRESSIONBOOK_NAME})
    @PostMapping
    public ResponseEntity<RsData<ExpressionBookResponse>> create(
        @RequestBody @Valid ExpressionBookRequest request,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        ExpressionBookResponse response = expressionBookService.create(request, memberId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new RsData<>(
                "200",
                "추가 표현함이 생성되었습니다.",
                response
            ));
    }

    /**
     * 추가 표현함 전체 조회
     *
     * @param userDetails 로그인한 사용자 정보
     * @return 표현함 목록을 담은 응답 객체
     */
    @Operation(summary = "표현함 전체 조회", description = "로그인한 사용자의 모든 추가 표현함을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "표현함 목록 조회에 성공했습니다.")
    @PossibleErrors({MEMBER_NOT_FOUND})
    @GetMapping
    public ResponseEntity<RsData<List<ExpressionBookResponse>>> getAllByMember(
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<ExpressionBookResponse> response = expressionBookService.getByMember(memberId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현함 목록 조회에 성공했습니다.",
                response
            ));
    }

    /**
     * 추가 표현함 이름 수정
     *
     * @param expressionBookId 수정할 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @param request          표현함 이름 수정 요청 객체
     * @return 수정 성공 응답
     */
    @Operation(summary = "표현함 이름 수정", description = "특정 추가 표현함의 이름을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "표현함 이름이 수정되었습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, FORBIDDEN_EXPRESSION_BOOK})
    @PatchMapping("/{expressionBookId}")
    public ResponseEntity<RsData<?>> updateName(
        @PathVariable Long expressionBookId,
        @Login CustomUserDetails userDetails,
        @RequestBody @Valid UpdateExpressionBookNameRequest request
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookService.updateName(expressionBookId, memberId, request.getNewName());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현함 이름이 수정되었습니다."
            ));
    }

    /**
     * 추가 표현함 삭제
     *
     * @param expressionBookId 삭제할 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @return 삭제 성공 응답
     */
    @Operation(summary = "표현함 삭제", description = "특정 추가 표현함을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "표현함이 삭제되었습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, FORBIDDEN_EXPRESSION_BOOK, EXPRESSIONBOOK_DELETE_DEFAULT_FORBIDDEN})
    @DeleteMapping("/{expressionBookId}")
    public ResponseEntity<RsData<?>> delete(
        @PathVariable Long expressionBookId,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookService.delete(expressionBookId, memberId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현함이 삭제되었습니다."
            ));
    }

    /**
     * 특정 표현함의 표현 목록 조회
     *
     * @param expressionBookId 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @return 표현 목록을 담은 응답 객체
     */
    @Operation(summary = "표현 목록 조회", description = "특정 표현함의 표현 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "표현함의 표현 목록 조회에 성공했습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, FORBIDDEN_EXPRESSION_BOOK, EXPRESSION_NOT_FOUND})
    @GetMapping("/{expressionBookId}/words")
    public ResponseEntity<RsData<List<ExpressionResponse>>> getExpressionsByBook(
        @PathVariable Long expressionBookId,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<ExpressionResponse> response = expressionBookService.getExpressionsByBook(expressionBookId, memberId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현함의 표현 목록 조회에 성공했습니다.",
                response
            ));
    }

    /**
     * 표현함에 표현 추가
     * 표현을 입력받아 품사/해석/난이도를 분석하여 반환한다.
     *
     * @param expressionBookId 표현함 ID
     * @param request          저장할 표현 데이터 요청 객체
     * @return 저장 결과 응답
     */
    @Operation(summary = "표현함에 표현 저장", description = "새 표현을 분석하고 저장합니다.")
    @ApiResponse(responseCode = "200", description = "표현이 저장되었습니다.")
    @PossibleErrors({EXPRESSION_BOOK_NOT_FOUND, VIDEO_ID_SEARCH_FAILED, GPT_RESPONSE_EMPTY})
    @PostMapping("/{expressionBookId}/expressions")
    public ResponseEntity<RsData<?>> saveExpression(
        @PathVariable("expressionBookId") Long expressionBookId,
        @RequestBody ExpressionSaveRequest request
    ) {
        expressionBookService.save(request, expressionBookId);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "표현함에 표현이 저장되었습니다."
        ));
    }

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
        expressionBookService.deleteExpressionsFromExpressionBook(request, memberId);

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
        expressionBookService.moveExpressions(request, memberId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new RsData<>(
                "200",
                "표현이 다른 표현함으로 이동되었습니다."
            ));
    }

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
        @RequestParam String keyword,
        @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<ExpressionResponse> result = expressionBookService.searchExpressions(memberId, keyword);
        return ResponseEntity.ok(new RsData<>(
            "200",
            "표현 검색 결과입니다.",
            result
        ));
    }
}

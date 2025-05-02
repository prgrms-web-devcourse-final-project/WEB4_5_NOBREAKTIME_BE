package com.mallang.mallang_backend.domain.sentence.expressionbook.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.UpdateExpressionBookNameRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/expressionbooks")
@RequiredArgsConstructor
@Tag(name = "ExpressionBook", description = "표현함 관리 API")
public class ExpressionBookController {

    private final ExpressionBookService expressionBookService;

    /**
     * 추가 표현함 생성
     *
     * @param request     표현함 이름과 언어 정보가 담긴 요청 객체
     * @param userDetails 로그인한 사용자 정보
     * @return 생성된 표현함 정보를 담은 응답 객체
     */
    @PostMapping
    @Operation(
        summary = "표현함 생성",
        description = "새로운 추가 표현함을 생성합니다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "표현함 생성 성공")
        }
    )
    public ResponseEntity<RsData<ExpressionBookResponse>> create(
        @RequestBody @Valid ExpressionBookRequest request,
        @Parameter(hidden = true) @Login CustomUserDetails userDetails
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
    @GetMapping
    @Operation(
        summary = "나의 표현함 조회",
        description = "로그인한 사용자가 가진 모든 추가 표현함 목록을 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    public ResponseEntity<RsData<List<ExpressionBookResponse>>> getAllByMember(
        @Parameter(hidden = true) @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<ExpressionBookResponse> response = expressionBookService.getByMember(memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "200",
                        "표현함 목록 조회 성공했습니다.",
                        response
                ));
    }

    /**
     * 추가 표현함 이름 수정
     *
     * @param expressionbookId 수정할 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @param request          표현함 이름 수정 요청 객체
     * @return 수정 성공 응답
     */
    @PatchMapping("/{expressionbookId}")
    @Operation(
        summary = "표현함 이름 수정",
        description = "특정 추가 표현함의 이름을 변경합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공")
        }
    )
    public ResponseEntity<RsData<?>> updateName(
        @Parameter(description = "수정할 표현함 ID", example = "42")
        @PathVariable Long expressionbookId,
        @Parameter(hidden = true) @Login CustomUserDetails userDetails,
        @RequestBody @Valid UpdateExpressionBookNameRequest request
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookService.updateName(expressionbookId, memberId, request.getNewName());
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
     * @param expressionbookId 삭제할 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{expressionbookId}")
    @Operation(
        summary = "표현함 삭제",
        description = "특정 추가 표현함을 삭제합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "삭제 성공")
        }
    )
    public ResponseEntity<RsData<?>> delete(
        @Parameter(description = "삭제할 표현함 ID", example = "42")
        @PathVariable Long expressionbookId,
        @Parameter(hidden = true) @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        expressionBookService.delete(expressionbookId, memberId);
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
     * @param expressionbookId 표현함 ID
     * @param userDetails      로그인한 사용자 정보
     * @return 표현 목록을 담은 응답 객체
     */
    @GetMapping("/{expressionbookId}/words")
    @Operation(
        summary = "표현함 내 표현 조회",
        description = "특정 추가 표현함에 저장된 표현 목록을 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    public ResponseEntity<RsData<List<ExpressionResponse>>> getExpressionsByBook(
        @Parameter(description = "조회할 표현함 ID", example = "42")
        @PathVariable Long expressionbookId,
        @Parameter(hidden = true) @Login CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        List<ExpressionResponse> response = expressionBookService.getExpressionsByBook(expressionbookId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "200",
                        "표현함의 표현 목록 조회 성공했습니다.",
                        response
                ));
    }

    /**
     * 단어를 입력받아 품사/해석/난이도를 분석하여 반환하는 API
     *
     * @param request - 표현/표현 해석
     * @return {표현}이 저장되었습니다.
     */
    @PostMapping("/{expressionbookId}/expressions")
    @Operation(
        summary = "표현 저장",
        description = "표현을 분석하여 특정 표현함에 저장합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
        }
    )
    public ResponseEntity<RsData> savedExpression(
        @Parameter(description = "저장할 표현함 ID", example = "42")
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

package com.mallang.mallang_backend.domain.sentence.expressionbook.controller;


import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.UpdateExpressionBookNameRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.ExpressionBookItemService;
import com.mallang.mallang_backend.global.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/expressionbooks")
@RequiredArgsConstructor
public class ExpressionBookController {

    private final ExpressionBookService expressionBookService;
    private final MemberRepository memberRepository;
    private final ExpressionBookItemService expressionBookItemService;

    /**
     * 추가 표현함 생성
     * @param request 표현함 이름과 언어 정보가 담긴 요청 객체
     * @param memberId 회원 ID
     * @return 생성된 표현함 정보를 담은 응답 객체
     */
    @PostMapping
    public ResponseEntity<RsData<ExpressionBookResponse>> create(
            @RequestBody @Valid ExpressionBookRequest request,
            @RequestParam Long memberId // 추후 @AuthenticationPrincipal 로 변경 해야할 것 같습니다.
    ) {
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
     * @param memberId 회원 ID
     * @return 표현함 목록을 담은 응답 객체
     */
    @GetMapping
    public ResponseEntity<RsData<List<ExpressionBookResponse>>> getAllByMember(
            @RequestParam Long memberId
    ) {
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
     * @param expressionbookId 수정할 표현함 ID
     * @param memberId 회원 ID
     * @param request 표현함 이름 수정 요청 객체
     * @return 수정 성공 응답
     */
    @PatchMapping("/{expressionbookId}")
    public ResponseEntity<RsData<?>> updateName(
            @PathVariable Long expressionbookId,
            @RequestParam Long memberId,
            @RequestBody @Valid UpdateExpressionBookNameRequest request
    ) {
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
     * @param expressionbookId 삭제할 표현함 ID
     * @param memberId 회원 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{expressionbookId}")
    public ResponseEntity<RsData<?>> delete(
            @PathVariable Long expressionbookId,
            @RequestParam Long memberId
    ) {
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
     * @param expressionbookId 표현함 ID
     * @param memberId 회원 ID
     * @return 표현 목록을 담은 응답 객체
     */
    @GetMapping("{expressionbookId}/words")
    public ResponseEntity<RsData<List<ExpressionResponse>>> getExpressionsByBook(
            @PathVariable Long expressionbookId,
            @RequestParam Long memberId
    ) {
        List<ExpressionResponse> response = expressionBookService.getExpressionsByBook(expressionbookId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "200",
                        "표현함의 표현 목록 조회 성공했습니다.",
                        response
                ));

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

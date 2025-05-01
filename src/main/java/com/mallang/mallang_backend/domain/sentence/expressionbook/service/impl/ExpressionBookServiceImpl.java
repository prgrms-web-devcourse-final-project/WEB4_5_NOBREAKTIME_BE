package com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionBookServiceImpl implements ExpressionBookService {

    private final ExpressionBookRepository expressionBookRepository;
    private final MemberRepository memberRepository;
    private final ExpressionBookItemRepository expressionBookItemRepository;
    private final ExpressionRepository expressionRepository;

    @Override
    @Transactional
    public ExpressionBookResponse create(ExpressionBookRequest request, Long memberId) {
        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        // 표현함 생성
        ExpressionBook expressionBook = ExpressionBook.builder()
                .name(request.getName())
                .language(request.getLanguage())
                .member(member)
                .build();

        expressionBookRepository.save(expressionBook);

        return ExpressionBookResponse.from(expressionBook);
    }

    @Transactional
    @Override
    public List<ExpressionBookResponse> getByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        List<ExpressionBook> books = expressionBookRepository.findAllByMember(member);

        return books.stream()
                .map(ExpressionBookResponse::from)
                .toList();
    }

    @Transactional
    @Override
    public void updateName(Long expressionBookId, Long memberId, String newName) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        // 엔티티 내부 메서드로 이름 수정
        book.updateName(newName);
    }

    @Transactional
    @Override
    public void delete(Long expressionBookId, Long memberId) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        expressionBookRepository.delete(book);
    }

    @Transactional
    @Override
    public List<ExpressionResponse> getExpressionsByBook(Long expressionBookId, Long memberId) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        List<ExpressionBookItem> items = expressionBookItemRepository.findAllById_ExpressionBookId(expressionBookId);

        return items.stream()
                .map(item -> expressionRepository.findById(item.getId().getExpressionId())
                        .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_NOT_FOUND))
                )
                .map(ExpressionResponse::from)
                .toList();
    }
}

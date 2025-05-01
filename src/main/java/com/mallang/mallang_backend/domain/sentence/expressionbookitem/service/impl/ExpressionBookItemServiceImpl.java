package com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.AddExpressionToBookListRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.AddExpressionToBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.DeleteExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.MoveExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.ExpressionBookItemService;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionBookItemServiceImpl implements ExpressionBookItemService {

    private final ExpressionBookRepository expressionBookRepository;
    private final ExpressionRepository expressionRepository;
    private final ExpressionBookItemRepository expressionBookItemRepository;
    private final VideoRepository videoRepository;

    @Transactional
    @Override
    public void addExpressionsFromVideo(Long expressionBookId, AddExpressionToBookListRequest request, Member member) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(member.getId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        for (AddExpressionToBookRequest dto : request.getExpressions()) {
            Videos videos = videoRepository.findById(dto.getVideoId())
                    .orElseThrow(() -> new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED));

            Expression expression = Expression.builder()
                    .sentence(dto.getSentence())
                    .description(dto.getDescription())
                    .sentenceAnalysis(dto.getSentenceAnalysis())
                    .subtitleAt(LocalTime.parse(dto.getSubtitleAt()))
                    .videos(videos)
                    .build();

            expressionRepository.save(expression);

            ExpressionBookItem item = ExpressionBookItem.builder()
                    .expressionId(expression.getId())
                    .expressionBookId(expressionBookId)
                    .build();

            expressionBookItemRepository.save(item);
        }
    }

    @Transactional
    @Override
    public void deleteExpressionsFromBook(DeleteExpressionsRequest request) {
        ExpressionBook book = expressionBookRepository.findById(request.getExpressionBookId())
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(request.getMemberId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        List<ExpressionBookItemId> ids = request.getExpressionIds().stream()
                .map(expressionId -> new ExpressionBookItemId(expressionId, request.getExpressionBookId()))
                .toList();

        expressionBookItemRepository.deleteAllById(ids);
    }

    @Transactional
    @Override
    public void moveExpressions(MoveExpressionsRequest request) {
        Long memberId = request.getMemberId();

        ExpressionBook sourceBook = expressionBookRepository.findById(request.getSourceExpressionBookId())
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        ExpressionBook targetBook = expressionBookRepository.findById(request.getTargetExpressionBookId())
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!sourceBook.getMember().getId().equals(memberId) ||
                !targetBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        List<ExpressionBookItemId> deleteIds = request.getExpressionIds().stream()
                .map(expressionId -> new ExpressionBookItemId(expressionId, sourceBook.getId()))
                .toList();

        expressionBookItemRepository.deleteAllById(deleteIds);

        for (Long expressionId : request.getExpressionIds()) {
            ExpressionBookItemId newId = new ExpressionBookItemId(expressionId, targetBook.getId());

            if (expressionBookItemRepository.existsById(newId)) continue;

            ExpressionBookItem newItem = ExpressionBookItem.builder()
                    .expressionId(expressionId)
                    .expressionBookId(targetBook.getId())
                    .build();

            expressionBookItemRepository.save(newItem);
        }
    }
}

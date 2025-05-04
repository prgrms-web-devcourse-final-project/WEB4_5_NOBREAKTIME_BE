package com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionBookResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.savedExpressionsRequest;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_EXPRESSION_BOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.EXPRESSIONBOOK_DELETE_DEFAULT_FORBIDDEN;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpressionBookServiceImpl implements ExpressionBookService {

    private final ExpressionBookRepository expressionBookRepository;
    private final MemberRepository memberRepository;
    private final ExpressionBookItemRepository expressionBookItemRepository;
    private final ExpressionRepository expressionRepository;
    private final VideoRepository videoRepository;
    private final GptService gptService;

    @Override
    @Transactional
    public ExpressionBookResponse create(ExpressionBookRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (!member.getSubscription().isStandardOrHigher()) {
            throw new ServiceException(ErrorCode.NO_EXPRESSIONBOOK_CREATE_PERMISSION);
        }

        if (request.getName().equals(DEFAULT_EXPRESSION_BOOK_NAME)) {
            throw new ServiceException(ErrorCode.EXPRESSIONBOOK_CREATE_DEFAULT_FORBIDDEN);
        }

        ExpressionBook expressionBook = ExpressionBook.builder()
                .name(request.getName())
                .language(request.getLanguage())
                .member(member)
                .build();

        expressionBookRepository.save(expressionBook);

        return ExpressionBookResponse.from(expressionBook);
    }

    @Override
    @Transactional
    public List<ExpressionBookResponse> getByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        List<ExpressionBook> books = expressionBookRepository.findAllByMember(member);

        return books.stream()
                .map(ExpressionBookResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateName(Long expressionBookId, Long memberId, String newName) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        book.updateName(newName);
    }

    @Override
    @Transactional
    public void delete(Long expressionBookId, Long memberId) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        if (DEFAULT_EXPRESSION_BOOK_NAME.equals(book.getName())) {
            throw new ServiceException(EXPRESSIONBOOK_DELETE_DEFAULT_FORBIDDEN);
        }

        expressionBookItemRepository.deleteAllByExpressionBookId(expressionBookId);

        expressionBookRepository.delete(book);
    }

    @Override
    @Transactional
    public List<ExpressionResponse> getExpressionsByBook(Long expressionBookId, Long memberId) {
        ExpressionBook book = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        if (!book.getMember().getId().equals(memberId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN_EXPRESSION_BOOK);
        }

        List<ExpressionBookItem> items = expressionBookItemRepository.findAllById_ExpressionBookId(expressionBookId);

        return items.stream()
                .map(item -> expressionRepository.findById(item.getId().getExpressionId())
                        .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_NOT_FOUND)))
                .map(ExpressionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void save(savedExpressionsRequest request, Long expressionbookId) {
        String videoId = request.getVideoId();
        String sentence = request.getSentence();
        String description = request.getDescription();
        LocalTime subtitleAt = request.getSubtitleAt();

        ExpressionBook expressionBook = expressionBookRepository.findById(expressionbookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        Expression expression = getOrCreateExpression(videoId, sentence, description, subtitleAt);

        ExpressionBookItemId itemId = new ExpressionBookItemId(expression.getId(), expressionBook.getId());
        if (!expressionBookItemRepository.existsById(itemId)) {
            expressionBookItemRepository.save(new ExpressionBookItem(itemId.getExpressionId(), itemId.getExpressionBookId()));
        }
    }

    private Expression getOrCreateExpression(String videoId, String sentence, String description, LocalTime subtitleAt) {
        return expressionRepository
                .findByVideosIdAndSentenceAndSubtitleAt(videoId, sentence, subtitleAt)
                .orElseGet(() -> {
                    String gptResult = gptService.analyzeSentence(sentence, description);
                    if (gptResult == null || gptResult.isBlank()) {
                        throw new ServiceException(ErrorCode.GPT_RESPONSE_EMPTY);
                    }

                    Videos video = videoRepository.findById(videoId)
                            .orElseThrow(() -> new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED));

                    return expressionRepository.save(Expression.builder()
                            .sentence(sentence)
                            .description(description)
                            .sentenceAnalysis(gptResult)
                            .videos(video)
                            .subtitleAt(subtitleAt)
                            .build());
                });
    }
}

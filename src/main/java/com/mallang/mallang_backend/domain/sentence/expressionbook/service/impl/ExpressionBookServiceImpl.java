package com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpressionBookServiceImpl implements ExpressionBookService {

    private final ExpressionBookRepository expressionBookRepository;
    private final ExpressionBookItemRepository expressionBookItemRepository;
    private final GptService gptService;
    private final ExpressionRepository expressionRepository;
    private final VideoRepository videoRepository;

    @Override
    @Transactional
    public void save(savedExpressionsRequest request, Long expressionbookId) {
        String videoId = request.getVideoId();
        String sentence = request.getSentence();
        String description = request.getDescription();
        LocalTime subtitleAt = request.getSubtitleAt();

        // 1. 표현북 조회
        ExpressionBook expressionBook = expressionBookRepository.findById(expressionbookId)
                .orElseThrow(() -> new ServiceException(ErrorCode.EXPRESSION_BOOK_NOT_FOUND));

        // 2. 표현 조회 또는 생성
        Expression expression = getOrCreateExpression(videoId, sentence, description, subtitleAt);

        // 3. 표현함에 연결 여부 확인 후 저장
        ExpressionBookItemId itemId = new ExpressionBookItemId(expression.getId(), expressionBook.getId());
        if (!expressionBookItemRepository.existsById(itemId)) {
            expressionBookItemRepository.save(new ExpressionBookItem(itemId.getExpressionId(), itemId.getExpressionBookId()));
        }
}

    /**
     * 기존 Expression이 존재하면 반환하고, 없으면 GPT 호출 후 생성하여 반환
     */
    private Expression getOrCreateExpression(String videoId, String sentence, String description, LocalTime subtitleAt) {
        return expressionRepository
                .findByVideosIdAndSentenceAndSubtitleAt(videoId, sentence, subtitleAt)
                .orElseGet(() -> {
                    // GPT 분석
                    String gptResult = gptService.analyzeSentence(sentence, description);
                    if (gptResult == null || gptResult.isBlank()) {
                        throw new ServiceException(ErrorCode.GPT_RESPONSE_EMPTY);
                    }

                    // 영상 존재 여부 확인
                    Videos video = videoRepository.findById(videoId)
                            .orElseThrow(() -> new ServiceException(ErrorCode.VIDEO_ID_SEARCH_FAILED));

                    // 표현 저장
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

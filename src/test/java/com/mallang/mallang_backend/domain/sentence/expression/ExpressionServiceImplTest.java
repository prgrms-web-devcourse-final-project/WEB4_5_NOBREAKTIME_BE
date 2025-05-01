package com.mallang.mallang_backend.domain.sentence.expression;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expression.service.impl.ExpressionServiceImpl;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.ExpressionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpressionServiceImplTest {

    private ExpressionRepository expressionRepository;
    private ExpressionServiceImpl expressionService;

    @BeforeEach
    void setUp() {
        expressionRepository = mock(ExpressionRepository.class);
        expressionService = new ExpressionServiceImpl(expressionRepository);
    }

    @Test
    @DisplayName("searchExpressions()는 키워드가 포함된 표현 리스트를 반환한다")
    void testSearchExpressions() {
        // given
        String keyword = "hello";

        Expression expression = Expression.builder()
                .sentence("Hello, how are you?")
                .description("인사 표현")
                .sentenceAnalysis("Hello (감탄사), how (부사), are (동사), you (대명사)")
                .subtitleAt(LocalTime.of(0, 1, 30))
                .videos(null) // videos는 null로 대체 가능 (테스트 목적)
                .build();

        when(expressionRepository.findBySentenceContainingIgnoreCase(keyword))
                .thenReturn(List.of(expression));

        // when
        List<ExpressionResponse> result = expressionService.searchExpressions(keyword);

        // then
        assertEquals(1, result.size());
        ExpressionResponse response = result.get(0);

        assertEquals("Hello, how are you?", response.getSentence());
        assertEquals("인사 표현", response.getDescription());
        assertEquals("Hello (감탄사), how (부사), are (동사), you (대명사)", response.getSentenceAnalysis());
        assertEquals("00:01:30", response.getSubtitleAt());

        verify(expressionRepository, times(1)).findBySentenceContainingIgnoreCase(keyword);
    }
}

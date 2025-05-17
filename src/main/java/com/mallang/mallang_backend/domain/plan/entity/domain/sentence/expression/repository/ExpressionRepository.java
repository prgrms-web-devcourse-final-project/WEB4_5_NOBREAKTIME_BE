package com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expression.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.sentence.expression.entity.Expression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExpressionRepository extends JpaRepository<Expression, Long> {
    List<Expression> findBySentenceContainingIgnoreCase(String keyword);

    Optional<Expression> findByVideosIdAndSentenceAndSubtitleAt(String videos_id, String sentence, LocalTime subtitleAt);

    List<Expression> findAllByVideosId(String videoId);
}

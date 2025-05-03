package com.mallang.mallang_backend.domain.sentence.expression.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;

public interface ExpressionRepository extends JpaRepository<Expression, Long> {
    List<Expression> findBySentenceContainingIgnoreCase(String keyword);

    Optional<Expression> findByVideosIdAndSentenceAndSubtitleAt(String videos_id, String sentence, LocalTime subtitleAt);

    List<Expression> findAllByVideosId(String videoId);
}

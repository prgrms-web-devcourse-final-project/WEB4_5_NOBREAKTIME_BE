package com.mallang.mallang_backend.domain.quiz.totalwordquiz.entity;


import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.QuizType;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 오늘의 학습 퀴즈 결과 -> 대시보드
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordQuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private WordQuiz wordQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "wordbook_id", referencedColumnName = "wordbook_id"),
            @JoinColumn(name = "word", referencedColumnName = "word")
    })
    private WordbookItem wordbookItem; // 복합 키 사용

    @Column(nullable = false)
    private Boolean isCorrect = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType; // 퀴즈유형
}

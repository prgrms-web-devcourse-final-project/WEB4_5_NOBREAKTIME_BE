package com.mallang.mallang_backend.domain.quiz.wordquizresult.entity;

import java.time.LocalDateTime;

import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordQuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_quiz_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_quiz_id", nullable = false)
    private WordQuiz wordQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordbook_item_id", nullable = false)
    private WordbookItem wordbookItem;

    @Column(nullable = false)
    private Boolean isCorrect = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public WordQuizResult(
        WordQuiz wordQuiz,
        WordbookItem wordbookItem,
        Boolean isCorrect
    ) {
        this.wordQuiz = wordQuiz;
        this.wordbookItem = wordbookItem;
        this.isCorrect = isCorrect;
    }
}

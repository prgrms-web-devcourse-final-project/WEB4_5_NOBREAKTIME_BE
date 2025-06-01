package com.mallang.mallang_backend.domain.quiz.wordquizresult.entity;

import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordQuizResult extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_quiz_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_quiz_id", nullable = false)
    private WordQuiz wordQuiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordbook_item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WordbookItem wordbookItem;

    @Column(nullable = false)
    private boolean isCorrect = false;

    @Builder
    public WordQuizResult(
        WordQuiz wordQuiz,
        WordbookItem wordbookItem,
        boolean isCorrect
    ) {
        this.wordQuiz = wordQuiz;
        this.wordbookItem = wordbookItem;
        this.isCorrect = isCorrect;
    }

    public void updateIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}

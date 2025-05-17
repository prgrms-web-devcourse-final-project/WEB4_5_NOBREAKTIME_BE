package com.mallang.mallang_backend.domain.quiz.wordquiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WordbookQuizResponse {
    private Long quizId;
    private String wordbookName;
    List<WordQuizItem> quizItems;
}

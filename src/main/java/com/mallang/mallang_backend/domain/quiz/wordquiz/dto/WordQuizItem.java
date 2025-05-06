package com.mallang.mallang_backend.domain.quiz.wordquiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WordQuizItem {
    private Long wordQuizItemId;
    private String word;
    private String question;
    private String original;
    private String translated;
}

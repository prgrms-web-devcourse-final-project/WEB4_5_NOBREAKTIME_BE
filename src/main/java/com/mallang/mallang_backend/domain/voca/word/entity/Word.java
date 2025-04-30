package com.mallang.mallang_backend.domain.voca.word.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;    // 단어(원어)

    @Column(nullable = false)
    private String pos;     // 품사

    @Column(nullable = false)
    private String meaning;     // 단어 뜻

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;  // 난이도

    @Column(nullable = false)
    private String exampleSentence; // 예문

    @Column(nullable = false)
    private String translatedSentence;  // 예문 번역

    @Builder
    public Word(
        String word,
        String pos,
        String meaning,
        Difficulty difficulty,
        String exampleSentence,
        String translatedSentence
    ) {
        this.word = word;
        this.pos = pos;
        this.meaning = meaning;
        this.difficulty = difficulty;
        this.exampleSentence = exampleSentence;
        this.translatedSentence = translatedSentence;
    }
}

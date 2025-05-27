package com.mallang.mallang_backend.global.gpt.service.impl;

import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GptPromptBuilderImpl 테스트")
class GptPromptBuilderImplTest {

    private final GptPromptBuilder builder = new GptPromptBuilderImpl();

    @Test
    @DisplayName("단어 검색 프롬프트가 지정 형식으로 생성된다")
    void buildPromptForSearchWordTest() {
        String word = "run";
        String result = builder.buildPromptForSearchWord(word);
        assertThat(result).contains("입력된 단어", word);
        assertThat(result).contains("품사", "해석", "예문");
    }

    @Test
    @DisplayName("문장 분석 프롬프트가 지정 형식으로 생성된다")
    void buildPromptForAnalyzeSentenceTest() {
        String sentence = "I like apples.";
        String translated = "나는 사과를 좋아한다.";
        String result = builder.buildPromptForAnalyzeSentence(sentence, translated);
        assertThat(result).contains(sentence);
        assertThat(result).contains(translated);
        assertThat(result).contains("숙어/표현:", "문법 구조:", "화용/의도:");
    }

    @Test
    @DisplayName("스크립트 분석 프롬프트가 지정 형식으로 생성된다")
    void buildPromptForAnalyzeScriptTest() {
        String script = "This is a test script.";
        String result = builder.buildPromptForAnalyzeScript(script);
        assertThat(result).contains("입력:", script);
    }

    @Test
    @DisplayName("레벨 테스트 프롬프트가 지정 형식으로 생성된다")
    void buildPromptForLevelTestScriptTest() {
        String wordLevel = "A";
        String expLevel = "B";
        String wordResult = "run | EASY | true";
        String expResult = "I like apples. | true";

        String result = builder.buildPromptForLevelTestScript(wordLevel, expLevel, wordResult, expResult);
        assertThat(result).contains(wordLevel, expLevel);
        assertThat(result).contains(wordResult);
        assertThat(result).contains(expResult);
        assertThat(result).contains("어휘 레벨 결과:", "표현 레벨 결과:");
    }
}

package com.mallang.mallang_backend.global.gpt.service;

public interface GptPromptBuilder {
	String buildPromptforSearchWord(String word);

	String buildPromptforSearchWordJapanese(String word);

	String buildPromptForAnalyzeSentence(String sentence, String translatedSentence);

	String buildPromptForAnalyzeSentenceJapanese(String sentence, String translatedSentence);

	String buildPromptForAnalyzeScript(String script);

	String buildPromptForAnalyzeScriptJapanese(String script);

	String buildPromptForLevelTestScript(String wordLevel, String expressionLevel, String wordQuizResult, String expressionQuizResult);

}

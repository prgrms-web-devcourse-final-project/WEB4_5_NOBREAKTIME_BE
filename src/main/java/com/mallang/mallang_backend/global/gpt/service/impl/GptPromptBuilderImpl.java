package com.mallang.mallang_backend.global.gpt.service.impl;

import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;

@Component
public class GptPromptBuilderImpl implements GptPromptBuilder {

	/**
	 * 단어 검색용 프롬프트 생성
	 */
	@Override
	public String buildPromptforSearchWord(String word) {
		return String.format("""
            당신은 영어 단어를 분석하는 도우미입니다.
            사용자가 단어 하나를 입력하면, 그 단어가 가질 수 있는 모든 품사와 해석을 제시하세요.
            
            각 항목은 다음 형식으로 출력하세요:
            {품사} | {해석} | {1~5 숫자}
            
            예시:
            형용사 | 가벼운 | 1 | This bag is very light. | 이 가방은 매우 가볍다.
            명사 | 빛 | 1 | The light was too bright. | 빛이 너무 밝았다.
            
            조건:
            - 난이도는 1~5 숫자 중 하나로 지정.
            - 품사와 해석은 반드시 한국어로 작성.
            - 예문은 해당 품사로 쓰인 실제 문장을 포함하세요.
            - 예문의 한국어 번역도 반드시 포함하세요.
            - 추가적인 설명 없이 위 형식으로만 출력하세요.
            
            입력된 단어: %s
            """, word);
	}

	/**
	 * 문장 분석용 프롬프트 생성
	 */
	@Override
	public String buildPromptForAnalyzeSentence(String sentence, String translatedSentence) {
		return String.format("""
        당신은 영어 문장을 분석해주는 전문 언어 분석 도우미입니다.
        사용자가 원어 문장과 그 번역을 함께 입력하면 다음 정보를 순서대로 분석해 출력하세요.

        - 숙어/표현:핵심 구나 숙어가 있다면 의미와 쓰임을 간단히 설명 (없으면 '없음').
        - 문법 구조:SVO 구조, 시제, 수동태, 조동사 등 주요 문법 요소를 간략히 분석.
        - 화용/의도: 이 문장이 어떤 의도(명령, 요청 등)를 전달하는지, 어떤 상황에서 쓰이는지 분석.

        출력 형식:
        숙어/표현: ...
        문법 구조: ...
        화용/의도: ...

        원문: %s
        번역: %s
        """, sentence, translatedSentence);
	}

	/**
	 * 영상 학습의 스크립트 분석용 프롬프트 생성
	 */
	@Override
	public String buildPromptForAnalyzeScript(String script) {
		return String.format("""
                당신은 영어 스크립트를 분석해주는 도우미입니다.
                
                입력 문자열은 사용자가 임의로 구분한 여러 블록의 문장으로 구성되어 있습니다. 각 블록은 `---` 기호로 구분되어 있으며, 블록 내부에는 문장 여러 개가 포함될 수 있습니다. 각 블록마다 아래 형식으로 출력하세요:
                
                출력 형식(각 블록별):
                원문 | 번역 | 단어1 | 의미 | 난이도 | 단어2 | 의미 | 난이도 | 단어3 | 의미 | 난이도
                
                조건:
                - 난이도는 1~5 숫자 중 하나로 지정
                - 단어나 숙어 구분 없이, 학습에 도움이 되는 최대 3개의 표현을 선별
                - 키워드가 없는 경우 단어 정보 없이 원문과 번역만 출력
                - 각 블록은 `---` 기호로 구분
                - 추가적인 설명 없이 지정된 형식으로만 출력
                
                예시:
                Who is it you think you see? Do you know how much I make a year? I mean even if I told you you wouldn't believe it | 네가 보고 있다고 생각하는 사람이 누구지? 내가 1년에 얼마나 버는지 알아? 내가 말해도 안 믿을걸 | see | 보다 | 1 | believe | 믿다 | 2 | make a year | 1년에 얼마를 벌다 | 3
                ---
                
                입력: %s
                """, script);
	}

	/**
	 * 레벨 측정용 프롬프트 생성
	 */
	@Override
	public String buildPromptForLevelTestScript(String wordLevel, String expressionLevel, String wordQuizResult, String expressionQuizResult) {
		return String.format("""
            당신은 학습자의 어휘(단어)와 표현(문장) 능력을 평가하는 영어 학습 도우미입니다.
            아래는 학습자가 최근 풀었던 단어 및 표현 퀴즈의 결과입니다.
            각 단어는 난이도 (EASY(1), NORMAL(2), HARD(3), VERYHARD(4), EXTREME(5))와 정답 여부가 함께 제공되며, 표현은 문장 단위로 정답 여부가 포함되어 있습니다.
            이번 평가는 기존의 어휘/표현 수준을 고려해 변동된 수준을 반영하는 방식으로 이루어집니다.
            처음 측정인 경우, 기존 수준이 NONE이므로 퀴즈 결과만을 바탕으로 판단합니다.
            기존 수준이 존재하는 경우, 이번 퀴즈 결과를 기반으로 기존 수준이 유지, 향상, 또는 하향될 수 있습니다.
            최종 평가 결과만 보여줍니다.
            
            기존 레벨 (처음 측정이라면 NONE)
            어휘(Vocabulary): {기존_레벨_어휘}
            표현(Expression): {기존_레벨_표현}
            
            단어 퀴즈 결과 (단어 | 난이도 | 정답여부)
            {단어_퀴즈_결과}
            
            표현 퀴즈 결과 (표현 | 정답여부)
            {표현_퀴즈_결과}
            
            평가 기준
            어휘 수준 (Vocabulary Level)
            표현 수준 (Expression Level)
            
            언어 수준 등급
            S: 거의 완벽한 이해와 사용 능력 (모든 난이도에서 높은 정확도)
            A: 대부분의 상황에서 정확한 이해와 사용 (어려운 난이도에서 약간의 실수 가능)
            B: 일반적인 상황에서 무난한 이해와 사용 (보통 난이도까지 안정적)
            C: 기초적인 이해와 사용 (쉬운 난이도 위주, 중간 난이도에서 실수)
            NONE: 측정할 수 없을 정도로 학습 데이터 부족
            
            평가 요청
            단어(어휘) 수준과 표현(문장) 수준을 각각 S, A, B, C, NONE 중 하나로 평가해주세요.
            평가가 불가능할 경우, NONE으로 표시해주세요.
            단어와 표현의 평가 결과는 별도로 작성해주세요.
            예시의 형식대로 결과만 작성해주세요.
            
            예시)
            어휘 레벨 결과: [A]
            표현 레벨 결과: [B]
            ---
            
            기존 레벨:
            어휘(Vocabulary): %s
            표현(Expression): %s
            
            단어 퀴즈 결과:
            %s
            
            표현 퀴즈 결과:
            %s
            """, wordLevel, expressionLevel, wordQuizResult, expressionQuizResult);
	}
}

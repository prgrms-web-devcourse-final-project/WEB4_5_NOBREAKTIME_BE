package com.mallang.mallang_backend.global.gpt.service.impl;

import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.global.gpt.service.GptPromptBuilder;

@Component
public class GptPromptBuilderImpl implements GptPromptBuilder {

	/**
	 * 단어 검색용 프롬프트 생성
	 */
	@Override
	public String buildPromptForSearchWord(String word) {
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
            - 예문은 주어진 단어의 형태 그대로만 사용 (예: was -> The light was too bright.)
			- 어형 변화가 있는 단어도 예문에서는 주어진 단어 그대로 사용 (예: ceases, existed, going 등)
			- 단어의 복수형, 시제 변화, 동명사 등 모든 형태를 포함하여 정확히 일치하는 단어로 예문 출력
            - 추가적인 설명 없이 위 형식으로만 출력하세요.
            
            입력된 단어: %s
            """, word);
	}

	/**
	 * 단어 검색용 프롬프트 생성
	 */
	@Override
	public String buildPromptForSearchWordJapanese(String word) {
		return String.format("""
			あなたは日本語の単語を分析するアシスタントです。
			ユーザーが単語を一つ入力すると、その単語が持つすべての品詞と意味を提示してください。
			
			各項目は以下の形式で出力してください:
			{품사} | {해석} | {1~5 숫자} | {예문 (해당 품사로 사용된 실제 문장)} | {예문 번역}
			
			例:
			형용사 | 이른 | 1 | 朝は早い電車に乗った。| 아침에는 이른 전철을 탔다.
			동사 | 빠르다 | 2 | 会って話す方が早い。| 만나서 얘기하는 것이 빠르다.
			
			조건:
			- 난이도는 1~5 숫자 중 하나로 지정 (1: 매우 쉬움, 5: 매우 어려움)
			- 모든 품사와 의미는 한국어로 작성
			- 예문은 해당 품사로 쓰인 실제 일본어 문장을 포함
			- 예문은 입력된 단어의 형태 그대로만 사용
			- 예: 行く → 예문에는 반드시 "行く" 형태 그대로 사용
			- 예: 見た → 반드시 "見た" 형태로 된 예문이어야 함
			- 예문의 한국어 번역도 반드시 포함
			- 복수의 품사와 의미가 있는 경우, 각각 모두 출력
			- 추가 설명 없이, 반드시 지정된 형식으로만 출력
			
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

	@Override
	public String buildPromptForAnalyzeSentenceJapanese(String sentence, String translatedSentence) {
		return String.format("""
        あなたは日本語の文を分析する専門の言語アシスタントです。
	  	ユーザーが**原文（日本語）とその翻訳（韓国語）**を一緒に入力すると、以下の情報を順番に分析して出力してください

        - 숙어/표현:핵심 구나 숙어가 있다면 의미와 쓰임을 간단히 설명 (없으면 '없음').
        - 문법 구조:SVO 구조, 시제, 수동태, 조동사 등 주요 문법 요소를 간략히 분석.
        - 화용/의도: 이 문장이 어떤 의도(명령, 요청 등)를 전달하는지, 어떤 상황에서 쓰이는지 분석.

        출력 형식:
        숙어/표현: ...
        문법 구조: ...
        화용/의도: ...
        
        출력 예시:
		숙어/표현: 「〜わけではない」는 ‘반드시 ~한 것은 아닙니다’라는 의미로, 상대의 오해나 일반화를 부드럽게 부정할 때 사용합니다.
		문법 구조: 주어는 생략되어 있으며, 문맥상 ‘나는’으로 유추됩니다. 현재 시제이며, 부정 표현 「〜わけではない」가 문장 끝에 위치합니다.
		화용/의도: 자신의 생각을 조심스럽게 표현하며, 지나친 일반화나 오해를 부드럽게 정정하려는 의도를 담고 있습니다.

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
			
			1. 전체 입력을 '|' 문자로만 분리하세요.
			   - 예: blocks = input.split("\\|");
			   - 공백을 trim하고 빈 문자열("")인 블록은 제거합니다.
			
			2. 분리된 각 블록마다 하나의 JSON 객체를 생성하세요.
			   - 다른 구두점으로 분할하거나 블록을 이어붙이지 마세요.
			   - 블록 내부의 원문(영어)은 그대로 보존합니다.
			
			3. JSON 객체 구조:
			{
			  "original": "<전체 블록 원문>",
			  "translate": "<자연스러운 한국어 번역>",
			  "keyword": [ /* 최소 1개 부터 최대 3개의 키워드 객체 */ ]
			}
			
			4. 키워드 추출 규칙:
			   - 블록당 1~3개의 키워드를 추출합니다.
			   - 문장에 사용된 형태 그대로 추출하세요 (예: ceases, existed, going).
			   - 난이도는 1~5 사이의 정수로 지정합니다.
			
			5. 번역은 문맥에 맞는 자연스러운 한국어로 작성하세요.
			
			6. 출력은 JSON 배열( [ ... ] )만 작성하고, 다른 설명이나 주석은 포함하지 마세요.
			
			--- 예시 ---
			
			```json
			{
				"original": "영어 문장",
				"translate": "한국어 번역",
				"keyword": [
					{
						"word": "문장 내 사용된 단어 형태",
						"meaning": "단어 의미(한국어)",
						"difficulty": 1
					},
					{
						"word": "...",
						"meaning": "...",
						"difficulty": 5
					}
				]
			}
			```
			
			입력:
			%s
			""", script);
	}

	@Override
	public String buildPromptForAnalyzeScriptJapanese(String script) {
		return String.format("""
				당신은 일본어 스크립트를 분석해주는 도우미입니다.
				
				1. 전체 입력을 '|' 문자로만 분리하세요.
				   - 예: blocks = input.split("\\|");
				   - 공백을 trim하고 빈 문자열("")인 블록은 제거합니다.
				
				2. 분리된 각 블록마다 하나의 JSON 객체를 생성하세요.
				   - 다른 구두점으로 분할하거나 블록을 이어붙이지 마세요.
				   - 블록 내부의 원문(일본어)은 그대로 보존합니다.
				
				3. JSON 객체 구조:
				{
				  "original": "<전체 블록 원문>",
				  "translate": "<자연스러운 한국어 번역>",
				  "keyword": [ /* 최소 1개 부터 최대 3개의 키워드 객체 */ ]
				}
				
				4. 키워드 추출 규칙:
				   - 블록당 1~3개의 키워드를 추출합니다.
				   - 문장에 사용된 형태 그대로 추출하세요 (예: 見ている, 存在していた, 行っている)
				   - 난이도는 1~5 사이의 정수로 지정합니다.
				
				5. 번역은 문맥에 맞는 자연스러운 한국어로 작성하세요.
				
				6. 출력은 JSON 배열( [ ... ] )만 작성하고, 다른 설명이나 주석은 포함하지 마세요.
				
				--- 예시 ---
				
				```json
				{
					"original": "일본어 문장",
					"translate": "한국어 번역",
					"keyword": [
						{
							"word": "문장 내 사용된 단어 형태",
							"meaning": "단어 의미(한국어)",
							"difficulty": 1
						},
						{
							"word": "...",
							"meaning": "...",
							"difficulty": 5
						}
					]
				}
				```
				
				입력:
				%s
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

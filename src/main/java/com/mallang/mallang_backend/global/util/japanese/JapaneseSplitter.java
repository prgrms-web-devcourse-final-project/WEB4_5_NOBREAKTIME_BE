package com.mallang.mallang_backend.global.util.japanese;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class JapaneseSplitter {
	public static String splitJapanese(String sentence) {
		Tokenizer tokenizer = new Tokenizer();
		List<Token> tokens = tokenizer.tokenize(sentence);
		List<String> segments = new ArrayList<>();
		StringBuilder segment = new StringBuilder();

		for (Token token : tokens) {
			segment.append(token.getSurface());

			boolean isParticle = "助詞".equals(token.getPartOfSpeechLevel1());
			boolean isPunctuation = "記号".equals(token.getPartOfSpeechLevel1()) &&
				("句点".equals(token.getPartOfSpeechLevel2()) || "読点".equals(token.getPartOfSpeechLevel2()));

			// 분할 기준이 되면 현재 segment를 하나의 단위로 저장
			if (isParticle || isPunctuation) {
				segments.add(segment.toString().trim());
				segment.setLength(0);
			}
		}
		// 마지막 남은 부분 추가
		if (segment.length() > 0) {
			segments.add(segment.toString().trim());
		}

		return String.join(" ", segments);
	}
}

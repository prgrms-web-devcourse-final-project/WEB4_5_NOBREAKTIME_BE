package com.mallang.mallang_backend.global.gpt.util;

import com.mallang.mallang_backend.domain.stt.converter.TranscriptSegment;
import com.mallang.mallang_backend.global.gpt.dto.GptSubtitleResponse;
import com.mallang.mallang_backend.global.gpt.dto.KeywordInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p> GPT 스크립트 분석 유틸 클래스</p>
 * <p>- 자막 전처리 (GPT 입력용) </p>
 * <p>- GPT 응답 파싱 → GptSubtitleResult 리스트로 변환</p>
 */
@Slf4j
public class GptScriptProcessor {

    /**
     * <p> GPT 프롬프트 생성을 위한 입력 문자열을 준비합니다. </p>
     * <p> 각 TranscriptSegment의 문장을 '|'로 이어붙입니다. </p>
     *
     * @param segments 자막 세그먼트 리스트
     * @return GPT 프롬프트용 입력 문자열 (ex. "문장1 | 문장2 | 문장3")
     */
    public static String prepareScriptInputText(List<TranscriptSegment> segments) {
        return segments.stream()
                .map(TranscriptSegment::getText)
                .collect(Collectors.joining(" | "));
    }

    /**
     * <p> GPT 응답 문자열을 파싱하여 GptSubtitleResult 리스트로 변환합니다. </p>
     * <p> 각 블록은 '---'로 구분되며, 각 줄은 '원문 | 번역 | 단어 | 의미 | 난이도 ...' 형식을 따라야 합니다. </p>
     *
     * @param gptResponse GPT로부터 받은 응답 문자열
     * @param segments GPT 요청 시 사용된 원본 자막 세그먼트 (시간/화자 정보를 포함)
     * @return 파싱된 GptSubtitleResult 리스트
     */
    public static List<GptSubtitleResponse> parseAnalysisResult(String gptResponse, List<TranscriptSegment> segments) {
        String[] blocks = gptResponse.strip().split("---");
        List<GptSubtitleResponse> results = new ArrayList<>();

        for(int i = 0; i < blocks.length ; i++){
            String block = blocks[i].strip();
            if(block.isBlank()) continue;

            String[] parts = block.split("\\|");
            if (parts.length < 2) {
                log.warn("잘못된 블록 형식 (최소 원문/번역 없음): {}", block);
                continue;
            }

            String  original = parts[0].strip();
            String translated = parts[1].strip();
            List<KeywordInfo> keywordInfos = new ArrayList<>();

            // 키워드가 있을 때만 처리
            if(parts.length > 2){
                for (int j = 2; j + 2 < parts.length; j += 3){
                    try {
                        String word = parts[j].strip();
                        String meaning = parts[j + 1].strip();
                        int difficulty = Integer.parseInt(parts[j + 2].strip());
                        keywordInfos.add(new KeywordInfo(word, meaning, difficulty));
                    } catch (Exception e) {
                        log.warn("키워드 파싱 실패 (index: {}): {}", j, e.getMessage());
                    }
                }
            }

            if(i >= segments.size()) break;
            TranscriptSegment seg = segments.get(i);

            GptSubtitleResponse dto = new GptSubtitleResponse(
                    seg.getStartTime(),
                    seg.getEndTime(),
                    seg.getSpeaker(),
                    original,
                    translated,
                    keywordInfos
            );

            results.add(dto);
        }
        return results;
    }
    
}

package com.mallang.mallang_backend.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GptMetricService {

    private final Counter sentenceSuccessCounter;
    private final Counter sentenceFailCounter;
    private final Counter wordSuccessCounter;
    private final Counter wordFailCounter;
    private final Counter scriptSuccessCounter;
    private final Counter scriptFailCounter;
    private final Counter apiSuccessCounter;
    private final Counter apiFailCounter;

    private final Counter loginCounter;
    private final Counter paymentCounter;
    private final Counter youtubeCallCounter;

    public GptMetricService(MeterRegistry meterRegistry) {
        // ==== GPT ==== //

        // 문장 분석 성공 카운터
        this.sentenceSuccessCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "success",
                "type", "sentence"
        );

        // 문장 분석 실패 카운터
        this.sentenceFailCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "fail",
                "type", "sentence"
        );

        // 단어 분석 성공 카운터
        this.wordSuccessCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "success",
                "type", "word"
        );

        // 단어 분석 실패 카운터
        this.wordFailCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "fail",
                "type", "word"
        );

        // 스크립트 분석 성공 카운터
        this.scriptSuccessCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "success",
                "type", "script"
        );

        // 스크립트 분석 실패 카운터
        this.scriptFailCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "fail",
                "type", "script"
        );

        // api 호출 성공 카운터
        this.apiSuccessCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "success",
                "type", "api"
        );

        // api 호출 실패 카운터
        this.apiFailCounter = meterRegistry.counter(
                "gpt_api_call_total",
                "result", "fail",
                "type", "api"
        );

        // 로그인 카운터
        this.loginCounter = meterRegistry.counter("member.login.count");
        // 결제 카운터
        this.paymentCounter = meterRegistry.counter("payment.call.count");
        // youtube 호출 카운터
        this.youtubeCallCounter = meterRegistry.counter("youtube.call.count");
    }

    /**
     * GPT 요청 성공 시
     */
    public void recordGptSentenceSuccess() {
        sentenceSuccessCounter.increment();
    }

    public void recordGptWordSuccess() {
        wordSuccessCounter.increment();
    }

    public void recordGptScriptSuccess() {
        scriptSuccessCounter.increment();
    }

    public void recordGptApiSuccess() {
        apiSuccessCounter.increment();
    }
    /**
     * GPT 요청 실패 시
     */
    public void recordGptSentenceFail() {
        sentenceFailCounter.increment();
    }

    public void recordGptWordFail() {
        wordFailCounter.increment();
    }

    public void recordGptScriptFail() {
        scriptFailCounter.increment();
    }

    public void recordGptApiFail() {
        apiFailCounter.increment();
    }

    // 로그인 시
    public void recordLogin() {
        loginCounter.increment();
    }

    // 결제 시
    public void recordPaymentCall () {
        paymentCounter.increment();
    }

    // youtube 호출 시
    public void recordYoutubeCall() {
        youtubeCallCounter.increment();
    }

    private int getActiveGptTokens() {
        // gpt 토큰 사용량 정보
        return 0;
    }
}
package com.mallang.mallang_backend.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetricService {

    private final Counter gptCallCounter;
    private final Counter loginCounter;
    private final Counter eventCounter;
    private final Counter paymentCounter;
    private final Counter youtubeCallCounter;

    public CustomMetricService(MeterRegistry meterRegistry) {
        // gpt 호출 카운터
        this.gptCallCounter = meterRegistry.counter("gpt.call.count");
        // 로그인 카운터
        this.loginCounter = meterRegistry.counter("member.login.count");
        // 결제 카운터
        this.paymentCounter = meterRegistry.counter("payment.call.count");
        // youtube 호출 카운터
        this.youtubeCallCounter = meterRegistry.counter("youtube.call.count");
        // 기본 이벤트 카운터
        this.eventCounter = meterRegistry.counter("custom.event.count");
    }

    // GPT 호출 시
    public void recordGptCall() {
        gptCallCounter.increment();
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

    // recordEvent()를 호출할 때마다 카운터가 1씩 증가
    public void recordEvent() {
        eventCounter.increment();
    }
}
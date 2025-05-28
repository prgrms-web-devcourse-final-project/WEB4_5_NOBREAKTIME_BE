package com.mallang.mallang_backend.global.resilience4j;

import com.mallang.mallang_backend.global.slack.SlackNotifier;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnIgnoredErrorEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.retry.event.RetryOnSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class CustomRetryConfigV2 {

    // 재시도 중 발생한 예외 추적
    private final ThreadLocal<List<Throwable>> retryExceptions = ThreadLocal.withInitial(ArrayList::new);
    private final SlackNotifier slackNotifier;

    public CustomRetryConfigV2(SlackNotifier slackNotifier) {
        this.slackNotifier = slackNotifier;
    }

    /**
     * Retry 이벤트 소비자 빈 등록
     */
    @Bean
    public RegistryEventConsumer<Retry> retryEventConsumer() {
        return new RetryEventConsumer();
    }

    /**
     * Retry 이벤트 소비자 구현
     */
    public class RetryEventConsumer implements RegistryEventConsumer<Retry> {

        @Override
        public void onEntryAddedEvent(EntryAddedEvent<Retry> event) {
            Retry retry = event.getAddedEntry();
            registerEventListeners(retry);
            log.debug("재시도 인스턴스 등록됨: {}", retry.getName());
        }

        @Override
        public void onEntryRemovedEvent(EntryRemovedEvent<Retry> event) {
            log.debug("재시도 인스턴스 제거됨: {}", event.getRemovedEntry().getName());
        }

        @Override
        public void onEntryReplacedEvent(EntryReplacedEvent<Retry> event) {
            Retry retry = event.getNewEntry();
            registerEventListeners(retry);
            log.debug("재시도 인스턴스 갱신됨: {}", retry.getName());
        }
    }

    /**
     * 이벤트 리스너 등록
     */
    private void registerEventListeners(Retry retry) {
        String name = retry.getName();
        try {
            retry.getEventPublisher()
                    .onRetry(event -> handleRetryEvent(name, event))
                    .onSuccess(event -> handleSuccessEvent(name, event))
                    .onError(event -> handleErrorEvent(name, event))
                    .onIgnoredError(event -> handleIgnoredErrorEvent(name, event));
        } catch (Exception e) {
            log.error("재시도 [{}] 이벤트 리스너 등록 실패", name, e);
        }
    }

    /**
     * 재시도 시도 이벤트 처리
     */
    private void handleRetryEvent(String name, RetryOnRetryEvent event) {
        // 예외 누적
        retryExceptions.get().add(event.getLastThrowable());

        // 트랜잭션 ID 추출
        String txId = MDC.get("transactionId");

        log.warn("[재시도][{}][txId={}] {}번째 시도 실패 - 예외: {} ({}) - 다음 시도 대기시간: {}ms",
                name,
                txId != null ? txId : "N/A",
                event.getNumberOfRetryAttempts(),
                event.getLastThrowable().getClass().getSimpleName(),
                event.getLastThrowable().getMessage(),
                event.getWaitInterval().toMillis());
    }

    /**
     * 재시도 성공 이벤트 처리
     */
    private void handleSuccessEvent(String name, RetryOnSuccessEvent event) {
        // 트랜잭션 ID 추출
        String txId = MDC.get("transactionId");

        // 소요 시간 계산
        ZonedDateTime startTime = event.getCreationTime();
        long durationMs = Duration.between(startTime, ZonedDateTime.now()).toMillis();

        log.info("[Retry][{}][txId={}] {}번 시도 후 성공 (소요시간: {}ms)",
                name,
                txId != null ? txId : "N/A",
                event.getNumberOfRetryAttempts(),
                durationMs);

        // 스레드 로컬 데이터 정리
        retryExceptions.remove();
    }

    /**
     * 재시도 최종 실패 이벤트 처리
     */
    private void handleErrorEvent(String name, RetryOnErrorEvent event) {
        // 마지막 예외 추가
        retryExceptions.get().add(event.getLastThrowable());

        // 트랜잭션 ID 추출
        String txId = MDC.get("transactionId");

        // 누적 예외 정보 수집
        List<String> errorDetails = retryExceptions.get().stream()
                .map(t -> String.format("%s: %s",
                        t.getClass().getSimpleName(),
                        t.getMessage()))
                .collect(Collectors.toList());

        log.error("[Fallback][{}][txId={}] {}번 시도 후 최종 실패 - 발생 예외 목록: {}",
                name,
                txId != null ? txId : "N/A",
                event.getNumberOfRetryAttempts(),
                errorDetails);

        String slackMessage = getSlackMessage(name, event, txId, errorDetails);

        slackNotifier.sendSlackNotification(
                "[Fallback] 재시도 중 최종 실패 발생",
                slackMessage
        );

        // 스레드 로컬 데이터 정리
        retryExceptions.remove();
    }

    private String getSlackMessage(String name, RetryOnErrorEvent event, String txId, List<String> errorDetails) {
        // Slack 알림 전송
        String query = String.format("transactionId : \"%s\" ", txId); // 띄어쓰기 포함
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8); // URL 인코딩
        String kibanaBaseUrl = "https://api.mallang.site:5601/app/discover#/"
                + "?_g=(filters:!(),query:(language:kuery,query:''),"
                + "refreshInterval:(pause:!t,value:60000),time:(from:now-30m,to:now))"
                + "&_a=(columns:!(),dataSource:(dataViewId:'17441b64-b5d7-45f4-aa21-8c31c4cc754b',type:dataView),"
                + "filters:!(),interval:auto,query:(language:kuery,query:'%s'),"
                + "sort:!(!(timestamp,desc)))";

        String kibanaLink = txId != null
                ? String.format(kibanaBaseUrl, encodedQuery)
                : "트랜잭션 ID 없음";

        return String.format(
                     "\n -  *서비스 이름*: `%s`\n" +
                        "-  *트랜잭션 ID*: `%s`\n" +
                        "-  *키바나에서 바로 보기*: <%s|로그 검색하기>\n" +
                        "-  *재시도 시도 횟수*: `%d`\n" +
                        "-  *마지막 예외*: `%s: %s`\n" +
                        "-  *누적 예외 목록:*\n```%s```\n" +
                        "⚠️ 신속한 확인이 필요합니다.",
                name,
                txId != null ? txId : "N/A",
                txId != null ? kibanaLink : "트랜잭션 ID 없음",
                event.getNumberOfRetryAttempts(),
                event.getLastThrowable().getClass().getSimpleName(),
                event.getLastThrowable().getMessage(),
                String.join("\n", errorDetails)
        );
    }

    /**
     * 무시된 에러 이벤트 처리
     */
    private void handleIgnoredErrorEvent(String name, RetryOnIgnoredErrorEvent event) {
        String txId = MDC.get("transactionId");
        Throwable t = event.getLastThrowable();

        log.warn("[재시도][{}][txId={}] 무시된 예외 발생 - 타입: {}, 메시지: {}",
                name,
                txId != null ? txId : "N/A",
                t.getClass().getName(),
                t.getMessage());
    }

}

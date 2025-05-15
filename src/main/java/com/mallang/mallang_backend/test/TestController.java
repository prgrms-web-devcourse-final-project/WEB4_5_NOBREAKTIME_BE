package com.mallang.mallang_backend.test;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.global.aop.monitor.MeasureExecutionTime;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final MemberRepository memberRepository;
    private final TestService testService;

    @Value("${slack.webhook.url}")
    private String webhookUrl;


    /**
     * 로그인 테스트 컨트롤러입니다.
     */
    @GetMapping("/api/test")
    public String test(@Login CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        Member findMember = memberRepository.findById(memberId).get();

        return "로그인 성공, 사용자 아이디 값: " + findMember.getId() +
                ", 닉네임: " + findMember.getNickname();

    }


    /**
     * GPT 서비스 테스트 호출용 API 엔드포인트
     */
    @GetMapping("/test/gpt")
    public String testGptService() {
        try {
            testService.testGptService(); // GPT 서비스 테스트 메서드 호출
            return "GPT 서비스 테스트가 성공적으로 완료되었습니다.";
        } catch (Exception e) {
            return "GPT 서비스 테스트 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 슬랙 알람 서비스 테스트 호출용 API 엔드포인트
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendSlackMessage(@RequestParam String message) {
        log.info("[SlackTest] 전송할 메시지: {}", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = String.format("{\"text\": \"%s\"}", message);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response =
                new RestTemplate().postForEntity(webhookUrl, request, String.class);

        return ResponseEntity.ok("슬랙 메시지 전송 완료: " + response.getStatusCode());
    }

    /**
     * 슬랙 알람 서비스 2XX외 에러 메시지 전송  test
     */
    @GetMapping("/api/test/error")
    public String triggerInternalServerError() {
        throw new RuntimeException("의도적으로 발생시킨 테스트용 500 오류");
    }

    @MeasureExecutionTime
    @GetMapping("/api/testMeasureExecutionTime")
    public String test() throws InterruptedException {
        Thread.sleep(200);
        return "ok";
    }
}

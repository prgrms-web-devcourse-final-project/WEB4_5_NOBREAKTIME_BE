package com.mallang.mallang_backend.global.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SlackNotifier {

    @Value("${custom.slack.webhook.url}")
    private String slackWebhookUrl;

    private final Slack slack = Slack.getInstance();

    public void sendSlackNotification(String message) {
        try {
            Payload payload = Payload.builder().text(message).build();
            slack.send(slackWebhookUrl, payload);
        } catch (IOException e) {
            throw new RuntimeException("슬랙 메시지 전송 실패", e);
        }
    }

    public void sendSlackNotification(String title, String message) {
        try {
            String formattedMessage = ":bell: *[" + title + "]*\n" + message;
            Payload payload = Payload.builder()
                    .text(formattedMessage)
                    .build();
            slack.send(slackWebhookUrl, payload);
        } catch (IOException e) {
            throw new RuntimeException("슬랙 메시지 전송 실패", e);
        }
    }
}
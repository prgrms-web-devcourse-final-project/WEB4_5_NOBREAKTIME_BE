package com.mallang.mallang_backend.global.slack;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SlackNotifierTest {

    @Autowired
    private SlackNotifier notifier;

    @Test
    @Disabled
    @DisplayName("실제 슬랙 알림 전송 테스트")
    void t1() throws Exception {
        notifier.sendSlackNotification("",
                "\n ");
    }
}
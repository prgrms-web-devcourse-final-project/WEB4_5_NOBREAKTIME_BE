package com.mallang.mallang_backend.domain.member.log.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OnlineUserCountLogger {

    private final RedisTemplate<String, String> redisTemplate;

    public OnlineUserCountLogger(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    public void logOnlineUserCount() {
        Long onlineCount = redisTemplate.opsForSet().size("online-users");
        log.info("ONLINE_USER_COUNT currentOnlineCount={}", onlineCount);
    }
}
package com.mallang.mallang_backend.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@Configuration
public class EmbeddedRedisConfig {

    private int redisPort = 6379;
    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        if (isPortInUse(redisPort)) {
            return;
        }
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    // 포트 사용 여부 확인
    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // 포트가 사용 중이 아니면 여기까지 옴
            return false;
        } catch (IOException e) {
            // 포트가 이미 사용 중이면 예외 발생
            return true;
        }
    }
}
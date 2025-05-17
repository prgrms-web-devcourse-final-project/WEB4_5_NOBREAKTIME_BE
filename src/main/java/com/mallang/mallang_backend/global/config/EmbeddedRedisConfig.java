package com.mallang.mallang_backend.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class EmbeddedRedisConfig {

    private int redisPort = 6379;
    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        if (isPortInUse("127.0.0.1", redisPort)) {
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
    private boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 200);
            return true; // 연결 성공 → 포트 사용 중
        } catch (IOException e) {
            return false; // 연결 실패 → 포트 사용 중 아님
        }
    }
}
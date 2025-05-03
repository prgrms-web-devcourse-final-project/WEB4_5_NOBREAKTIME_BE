package com.mallang.mallang_backend.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${server.env}")
    private String env; // blue or green

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.serverAddress}")
    private String serverAddress;

    @Value("${server.serverName}")
    private String serverName;

    // actuator: 애플리케이션을 모니터링하고 상호 작용할 수 있다
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("serverPort", serverPort);
        responseData.put("serverAddress", serverAddress);
        responseData.put("serverName", serverName);
        responseData.put("env", env);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/env")
    public ResponseEntity<?> getEnv() {
        return ResponseEntity.ok(env);
    }
}

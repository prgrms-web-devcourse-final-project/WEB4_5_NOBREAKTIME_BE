package com.mallang.mallang_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .build();
    }
}
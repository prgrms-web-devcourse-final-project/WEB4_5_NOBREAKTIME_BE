package com.mallang.mallang_backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {

    @Value("${toss.payment.APIKey}")
    private String paymentApiSecretKey;

    @Value("${toss.payment.widgetKey}")
    private String paymentWidgetKey;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .build();
    }

    @Bean
    public WebClient tossPaymentsSingleWebClient() {
        String encodedKey = Base64.getEncoder()
                .encodeToString((paymentWidgetKey + ":").getBytes());

        return WebClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encodedKey)
                .build();
    }

    @Bean
    public WebClient tossPaymentsBillingWebClient() {
        String encodedKey = Base64.getEncoder()
                .encodeToString((paymentApiSecretKey + ":").getBytes());

        return WebClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encodedKey)
                .build();
    }
}
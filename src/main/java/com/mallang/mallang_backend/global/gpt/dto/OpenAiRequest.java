package com.mallang.mallang_backend.global.gpt.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OpenAiRequest {
    private String model;
    private Message[] messages;
}

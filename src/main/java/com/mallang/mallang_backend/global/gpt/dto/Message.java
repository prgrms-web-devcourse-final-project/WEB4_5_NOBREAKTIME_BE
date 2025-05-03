package com.mallang.mallang_backend.global.gpt.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String role;
    private String content;
}

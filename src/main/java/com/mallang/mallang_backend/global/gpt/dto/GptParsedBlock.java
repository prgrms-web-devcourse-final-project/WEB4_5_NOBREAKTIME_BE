package com.mallang.mallang_backend.global.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptParsedBlock {
    private String original;
    private String translate;
    private List<KeywordInfo> keyword;
}
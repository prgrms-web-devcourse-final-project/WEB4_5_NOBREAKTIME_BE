package com.mallang.mallang_backend.domain.dashboard.dto;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class LevelCheckResponse {
	private String wordLevel;
	private String expressionLevel;
}

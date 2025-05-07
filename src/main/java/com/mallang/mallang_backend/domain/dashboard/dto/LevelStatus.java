package com.mallang.mallang_backend.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LevelStatus {
	private String word;
	private String expression;
	private LocalDateTime lastUpdated;
	// 재측정 가능한지
	private Boolean remeasurable;
}

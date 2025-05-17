package com.mallang.mallang_backend.domain.plan.entity.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class LevelCheckResponse {
	private String wordLevel;
	private String expressionLevel;
}

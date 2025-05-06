package com.mallang.mallang_backend.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningHistoryResponse {
	LearningHistory today;
	LearningHistory yesterday;
	LearningHistory week;
}

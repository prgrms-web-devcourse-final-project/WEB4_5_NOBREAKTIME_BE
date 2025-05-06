package com.mallang.mallang_backend.domain.dashboard.service;

import java.time.LocalDate;

import com.mallang.mallang_backend.domain.dashboard.dto.LearningHistoryResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.UpdateGoalRequest;

public interface DashboardService {
	StatisticResponse getStatistics(Long memberId);

	void updateGoal(UpdateGoalRequest request, Long memberId);

	LearningHistoryResponse getLearningStatisticsByPeriod(Long memberId, LocalDate now);
}

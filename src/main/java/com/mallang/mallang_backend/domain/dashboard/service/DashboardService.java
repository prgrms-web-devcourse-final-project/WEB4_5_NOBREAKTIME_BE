package com.mallang.mallang_backend.domain.dashboard.service;

import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;

public interface DashboardService {
	StatisticResponse getStatistics(Long memberId);
}

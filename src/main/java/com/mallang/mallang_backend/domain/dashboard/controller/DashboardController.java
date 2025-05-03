package com.mallang.mallang_backend.domain.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.Login;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	/**
	 * 대시보드에 포함된 정보들을 조회합니다.
	 * 인사, 하루 목표, 레벨
	 * @param userDetail 로그인한 사용자
	 * @return 대시보드 정보
	 */
	@GetMapping("/statistics")
	public ResponseEntity<RsData<StatisticResponse>> statistics(
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		StatisticResponse result = dashboardService.getStatistics(memberId);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"대시보드 조회에 성공했습니다.",
			result
		));
	}
}

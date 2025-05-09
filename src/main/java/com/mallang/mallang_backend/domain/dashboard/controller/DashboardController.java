package com.mallang.mallang_backend.domain.dashboard.controller;

import com.mallang.mallang_backend.domain.dashboard.dto.LearningHistoryResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelCheckResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.UpdateGoalRequest;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.mallang.mallang_backend.global.exception.ErrorCode.API_ERROR;
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Tag(name = "Dashboard", description = "대시보드 관련 API")
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
	@Operation(summary = "대시보드 조회", description = "대시보드 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "대시보드 조회에 성공했습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND})
	@GetMapping("/statistics")
	public ResponseEntity<RsData<StatisticResponse>> statistics(
		@Parameter(hidden = true)
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

	/**
	 * 하루의 학습 목표 설정(영상, 단어)
	 * @param request 설정할 영상, 단어 수
	 * @param userDetail 로그인한 사용자 정보
	 * @return 목표 설정 완료
	 */
	@Operation(summary = "학습 목표 설정", description = "영상 학습 목표, 단어 학습 목표를 설정합니다.")
	@ApiResponse(responseCode = "200", description = "학습 목표가 설정되었습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND})
	@PatchMapping("/goal")
	public ResponseEntity<RsData<Void>> updateGoal(
		@RequestBody UpdateGoalRequest request,
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		dashboardService.updateGoal(request, memberId);

		return ResponseEntity.ok(new RsData<>(
			"200",
			"학습 목표가 설정되었습니다."
		));
	}

	@Operation(summary = "기간별 학습 통계", description = "특정 기간에 대한 학습 통계 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "학습 통계 정보가 조회되었습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND})
	@GetMapping("/calendar")
	public ResponseEntity<RsData<LearningHistoryResponse>> getCalendarsData(
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		LearningHistoryResponse response = dashboardService.getLearningStatisticsByPeriod(memberId, LocalDate.now());

		return ResponseEntity.ok(new RsData<>(
			"200",
			"학습 통계 정보가 조회되었습니다.",
			response
		));
	}

	/**
	 * 사용자의 퀴즈 결과(통합 퀴즈, 단어장 퀴즈, 표현 퀴즈 결과)를 기반으로 학습 레벨을 측정합니다.
	 * @param userDetail 로그인한 회원
	 * @return 측정된 학습 레벨(어휘 레벨, 표현 레벨)
	 */
	@Operation(summary = "학습 레벨 측정", description = "최근 퀴즈 결과에 대한 학습 레벨을 측정합니다.")
	@ApiResponse(responseCode = "200", description = "학습 레벨이 측정되었습니다.")
	@PossibleErrors({MEMBER_NOT_FOUND, LEVEL_NOT_MEASURABLE, API_ERROR})
	@PostMapping("/level")
	public ResponseEntity<RsData<LevelCheckResponse>> levelCheck(
		@Parameter(hidden = true)
		@Login CustomUserDetails userDetail
	) {
		Long memberId = userDetail.getMemberId();

		LevelCheckResponse response = dashboardService.checkLevel(memberId);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"학습 레벨이 측정되었습니다.",
			response
		));
	}
}

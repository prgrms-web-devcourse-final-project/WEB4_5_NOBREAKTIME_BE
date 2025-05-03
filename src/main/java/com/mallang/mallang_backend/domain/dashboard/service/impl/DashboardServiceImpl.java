package com.mallang.mallang_backend.domain.dashboard.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.dashboard.dto.AchievementDetail;
import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelStatus;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.UpdateGoalRequest;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final MemberRepository memberRepository;
	private final VideoHistoryRepository videoHistoryRepository;
	private final WordQuizResultRepository wordQuizResultRepository;
	private final ExpressionQuizResultRepository expressionQuizResultRepository;

	@Override
	public StatisticResponse getStatistics(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

		int watchedVideoCount = videoHistoryRepository.countByMember(member);

		DailyGoal dailyGoal = calculateDailyGoal(member);
		LevelStatus levelStatus = calculateLevelStatus(member);

		return new StatisticResponse(
			member.getNickname(),
			watchedVideoCount,
			dailyGoal,
			levelStatus
		);
	}

	private DailyGoal calculateDailyGoal(Member member) {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		int wordCount = wordQuizResultRepository.countByWordQuiz_MemberAndCreatedAtAfter(member, todayStart);
		int videoCount = videoHistoryRepository.countByMemberAndCreatedAtAfter(member, todayStart);

		double wordAchievementRate = member.getWordGoal() == 0 ? 0 :
			Math.min(100.0, (double)wordCount / member.getWordGoal() * 100);
		double videoAchievementRate = member.getVideoGoal() == 0 ? 0 :
			Math.min(100.0, (double)videoCount / member.getVideoGoal() * 100);
		double achievementRate = Math.round((wordAchievementRate + videoAchievementRate) / 2 * 10) / 10.0;

		return new DailyGoal(
			member.getVideoGoal(),
			member.getWordGoal(),
			achievementRate,
			new AchievementDetail(videoCount, wordCount)
		);
	}

	private LevelStatus calculateLevelStatus(Member member) {
		return new LevelStatus(
			member.getWordLevel().getLabel(),
			member.getExpressionLevel().getLabel(),
			member.getCreatedAt(),
			checkMeasurable(member)
		);
	}

	private boolean checkMeasurable(Member member) {
		// 최소 측정인 경우 200개 이상의 문제를 풀었을 때 측정이 가능할 수 있다
		if (member.isFirstLevelMeasure()) {
			int wordQuizResultCount = wordQuizResultRepository.countByWordQuiz_Member(member);
			int expressionQuizResultCount = expressionQuizResultRepository.countByExpressionQuiz_Member(member);

			return 200 <= wordQuizResultCount + expressionQuizResultCount;
		}
		// 최초 측정이 아닌 경우 100개 이상의 문제를 풀었을 때 지금 측정 가능 버튼 활성화
		int wordQuizResultCount = wordQuizResultRepository.countByWordQuiz_MemberAndCreatedAtAfter(
			member, member.getMeasuredAt());
		int expressionQuizResultCount = expressionQuizResultRepository.countByExpressionQuiz_MemberAndCreatedAtAfter(
			member, member.getMeasuredAt());
		return 100 <= wordQuizResultCount + expressionQuizResultCount;
	}

	@Override
	@Transactional
	public void updateGoal(UpdateGoalRequest request, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
		member.updateVideoGoal(request.getVideoGoal());
		member.updateWordGoal(request.getWordGoal());
		memberRepository.save(member);
	}
}

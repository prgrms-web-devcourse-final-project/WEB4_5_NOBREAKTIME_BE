package com.mallang.mallang_backend.domain.dashboard.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.dashboard.dto.AchievementDetail;
import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelStatus;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final MemberRepository memberRepository;
	private final VideoHistoryRepository videoHistoryRepository;
	private final WordQuizResultRepository wordQuizResultRepository;

	@Override
	@Transactional
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
			Math.min(100.0, (double) wordCount / member.getWordGoal() * 100);
		double videoAchievementRate = member.getVideoGoal() == 0 ? 0 :
			Math.min(100.0, (double) videoCount / member.getVideoGoal() * 100);
		double achievementRate = (wordAchievementRate + videoAchievementRate) / 2;

		return new DailyGoal(
			member.getVideoGoal(),
			member.getWordGoal(),
			achievementRate,
			new AchievementDetail(videoCount, wordCount)
		);
	}

	private LevelStatus calculateLevelStatus(Member member) {
		// TODO: 추후 Member에 레벨 속성 추가 시 로직 변경
		return new LevelStatus(
			"D", "D", "D", "D",
			member.getCreatedAt(),
			false
		);
	}
}

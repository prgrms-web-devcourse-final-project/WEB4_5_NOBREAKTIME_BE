package com.mallang.mallang_backend.domain.dashboard.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.dashboard.dto.AchievementDetail;
import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.LearningHistory;
import com.mallang.mallang_backend.domain.dashboard.dto.LearningHistoryResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelStatus;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.UpdateGoalRequest;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.repository.ExpressionQuizRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.quiz.wordquiz.repository.WordQuizRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
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
	private final WordQuizRepository wordQuizRepository;
	private final ExpressionQuizRepository expressionQuizRepository;
	private final WordbookItemRepository wordbookItemRepository;

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

	@Override
	public LearningHistoryResponse getLearningStatisticsByPeriod(Long memberId, LocalDate now) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

		LocalDateTime todayStart = now.atStartOfDay();
		LocalDateTime yesterdayStart = todayStart.minusDays(1);
		LocalDateTime weekStart = todayStart.minusDays(6); // 오늘 포함 7일

		// 일주일 기준으로 공통 조회
		List<WordQuiz> wordQuizList = wordQuizRepository.findByMemberAndCreatedAtAfter(member, weekStart);
		List<ExpressionQuiz> expressionQuizList = expressionQuizRepository.findByMemberAndCreatedAtAfter(member, weekStart);
		List<WordQuizResult> wordQuizResults = wordQuizResultRepository.findByWordQuiz_MemberAndCreatedAtAfter(member, weekStart);
		List<ExpressionQuizResult> expressionQuizResults = expressionQuizResultRepository.findByExpressionQuiz_MemberAndCreatedAtAfter(member, weekStart);
		List<VideoHistory> videoHistories = videoHistoryRepository.findByMemberAndCreatedAtAfter(member, weekStart);
		List<WordbookItem> wordbookItems = wordbookItemRepository.findByWordbook_MemberAndCreatedAtAfter(member, weekStart);

		// 오늘
		LearningHistory today = createLearningHistory(todayStart, todayStart.plusDays(1),
			wordQuizList, expressionQuizList, wordQuizResults, expressionQuizResults,
			videoHistories, wordbookItems);

		// 어제
		LearningHistory yesterday = createLearningHistory(yesterdayStart, todayStart,
			wordQuizList, expressionQuizList, wordQuizResults, expressionQuizResults,
			videoHistories, wordbookItems);

		// 주간
		LearningHistory week = createLearningHistory(weekStart, todayStart.plusDays(1),
			wordQuizList, expressionQuizList, wordQuizResults, expressionQuizResults,
			videoHistories, wordbookItems);

		return new LearningHistoryResponse(today, yesterday, week);
	}

	private LearningHistory createLearningHistory(
		LocalDateTime from, LocalDateTime to,
		List<WordQuiz> wordQuizzes,
		List<ExpressionQuiz> expressionQuizzes,
		List<WordQuizResult> wordQuizResults,
		List<ExpressionQuizResult> expressionQuizResults,
		List<VideoHistory> videoHistories,
		List<WordbookItem> wordbookItems
	) {
		long learningSeconds = wordQuizzes.stream()
			.filter(wq -> isInRange(wq.getCreatedAt(), from, to))
			.mapToLong(WordQuiz::getLearningTime)
			.sum()
			+
			expressionQuizzes.stream()
				.filter(eq -> isInRange(eq.getCreatedAt(), from, to))
				.mapToLong(ExpressionQuiz::getLearningTime)
				.sum();

		int quizCount = (int) wordQuizResults.stream()
			.filter(r -> isInRange(r.getCreatedAt(), from, to))
			.count()
			+
			(int) expressionQuizResults.stream()
				.filter(r -> isInRange(r.getCreatedAt(), from, to))
				.count();

		int videoCount = (int) videoHistories.stream()
			.filter(v -> isInRange(v.getCreatedAt(), from, to))
			.count();

		int addedWordCount = (int) wordbookItems.stream()
			.filter(w -> isInRange(w.getCreatedAt(), from, to))
			.count();

		return new LearningHistory(formatDuration(learningSeconds), quizCount, videoCount, addedWordCount);
	}

	private boolean isInRange(LocalDateTime dateTime, LocalDateTime from, LocalDateTime to) {
		return (dateTime != null && !dateTime.isBefore(from) && dateTime.isBefore(to));
	}

	private String formatDuration(long totalSeconds) {
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}

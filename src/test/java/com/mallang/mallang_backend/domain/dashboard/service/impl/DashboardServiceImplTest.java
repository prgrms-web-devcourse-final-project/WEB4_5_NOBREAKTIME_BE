package com.mallang.mallang_backend.domain.dashboard.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelStatus;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.global.common.Language;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private VideoHistoryRepository videoHistoryRepository;

	@Mock
	private WordQuizResultRepository wordQuizResultRepository;

	@Mock
	private ExpressionQuizResultRepository expressionQuizResultRepository;

	@InjectMocks
	private DashboardServiceImpl dashboardServiceImpl;

	private Member member;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.nickname("TestUser")
			.language(Language.ENGLISH)
			.build();
		member.updateVideoGoal(3);
		member.updateWordGoal(5);
		member.updateMeasuredAt(LocalDateTime.now());
	}

	@Test
	@DisplayName("대시보드 정보를 조회할 수 있다")
	void getStatistics_shouldReturnCorrectStatisticResponse() {
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(videoHistoryRepository.countByMember(member)).thenReturn(100);
		when(videoHistoryRepository.countByMemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(2);
		when(wordQuizResultRepository.countByWordQuiz_MemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(4);
		when(expressionQuizResultRepository.countByExpressionQuiz_Member(eq(member)))
			.thenReturn(200);

		StatisticResponse response = dashboardServiceImpl.getStatistics(1L);

		assertThat(response.getUserName()).isEqualTo("TestUser");
		assertThat(response.getWatchedVideoCount()).isEqualTo(100);

		DailyGoal dailyGoal = response.getDailyGoal();
		assertThat(dailyGoal.getVideoGoal()).isEqualTo(3);
		assertThat(dailyGoal.getWordGoal()).isEqualTo(5);
		assertThat(dailyGoal.getAchievementDetail().getCompletedVideos()).isEqualTo(2);
		assertThat(dailyGoal.getAchievementDetail().getCompletedWords()).isEqualTo(4);
		LevelStatus levelStatus = response.getLevelStatus();
		assertThat(levelStatus.getRemeasurable()).isTrue();

		// 성취도는 (2/3 * 100 + 4/5 * 100) / 2 = (66.66 + 80) / 2 = 73.33 -> 소수점 1자리까지 반올림 73.3
		assertThat(dailyGoal.getAchievementRate()).isEqualTo(73.3);
	}

	@Test
	@DisplayName("첫 레벨 측정 시 푼 퀴즈가 200개 미만이면 재측정 불가능하다")
	void getStatistics_notMeasurable() {
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(videoHistoryRepository.countByMember(member)).thenReturn(100);
		when(wordQuizResultRepository.countByWordQuiz_Member(eq(member)))
			.thenReturn(0);
		when(expressionQuizResultRepository.countByExpressionQuiz_Member(eq(member)))
			.thenReturn(199);

		StatisticResponse response = dashboardServiceImpl.getStatistics(1L);

		assertThat(response.getLevelStatus().getRemeasurable()).isFalse();
	}
}

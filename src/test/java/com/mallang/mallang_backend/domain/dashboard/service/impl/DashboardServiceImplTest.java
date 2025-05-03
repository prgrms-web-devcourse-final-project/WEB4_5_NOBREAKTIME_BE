package com.mallang.mallang_backend.domain.dashboard.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
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
	}

	@Test
	void getStatistics_shouldReturnCorrectStatisticResponse() {
		// given
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();

		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(videoHistoryRepository.countByMember(member)).thenReturn(100);
		when(videoHistoryRepository.countByMemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(2);
		when(wordQuizResultRepository.countByWordQuiz_MemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(4);

		// when
		StatisticResponse response = dashboardServiceImpl.getStatistics(1L);

		// then
		assertThat(response.getUserName()).isEqualTo("TestUser");
		assertThat(response.getWatchedVideoCount()).isEqualTo(100);

		DailyGoal dailyGoal = response.getDailyGoal();
		assertThat(dailyGoal.getVideoGoal()).isEqualTo(3);
		assertThat(dailyGoal.getWordGoal()).isEqualTo(5);
		assertThat(dailyGoal.getAchievementDetail().getCompletedVideos()).isEqualTo(2);
		assertThat(dailyGoal.getAchievementDetail().getCompletedWords()).isEqualTo(4);

		// 성취도는 (2/3 * 100 + 4/5 * 100) / 2 = (66.66 + 80) / 2 = 73.33
		assertThat(dailyGoal.getAchievementRate()).isCloseTo(73.33, within(0.1));
	}
}

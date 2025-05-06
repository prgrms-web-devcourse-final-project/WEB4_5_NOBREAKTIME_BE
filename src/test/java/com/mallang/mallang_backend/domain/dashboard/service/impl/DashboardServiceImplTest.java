package com.mallang.mallang_backend.domain.dashboard.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mallang.mallang_backend.domain.dashboard.dto.DailyGoal;
import com.mallang.mallang_backend.domain.dashboard.dto.LearningHistoryResponse;
import com.mallang.mallang_backend.domain.dashboard.dto.LevelStatus;
import com.mallang.mallang_backend.domain.dashboard.dto.StatisticResponse;
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

	@Mock
	private WordQuizRepository wordQuizRepository;

	@Mock
	private ExpressionQuizRepository expressionQuizRepository;

	@Mock
	private WordbookItemRepository wordbookItemRepository;

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

	@Test
	@DisplayName("오늘, 어제, 일주일에 대한 학습 통계를 조회할 수 있다")
	void getLearningStatisticsByPeriod() {
		Long memberId = 1L;
		LocalDate now = LocalDate.of(2025, 4, 30);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(wordQuizRepository.findByMemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createWordQuiz(now, 120)));
		when(expressionQuizRepository.findByMemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createExpressionQuiz(now.plusDays(1), 180)));
		when(wordQuizResultRepository.findByWordQuiz_MemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createWordQuizResult(now)));
		when(expressionQuizResultRepository.findByExpressionQuiz_MemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createExpressionQuizResult(now.plusDays(1))));
		when(videoHistoryRepository.findByMemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createVideoHistory(now.plusDays(2))));
		when(wordbookItemRepository.findByWordbook_MemberAndCreatedAtAfter(eq(member), any()))
			.thenReturn(List.of(createWordbookItem(now)));

		LearningHistoryResponse response = dashboardServiceImpl.getLearningStatisticsByPeriod(memberId, now.plusDays(2));

		assertNotNull(response);
		assertEquals("00:05:00", response.getWeek().getLearningTime());
		assertEquals(2, response.getWeek().getQuizCount());
		assertEquals(1, response.getWeek().getVideoCount());
		assertEquals(1, response.getWeek().getAddedWordCount());

		assertEquals("00:03:00", response.getYesterday().getLearningTime());
		assertEquals(1, response.getYesterday().getQuizCount());
		assertEquals(0, response.getYesterday().getVideoCount());
		assertEquals(0, response.getYesterday().getAddedWordCount());

		assertEquals("00:00:00", response.getToday().getLearningTime());
		assertEquals(0, response.getToday().getQuizCount());
		assertEquals(1, response.getToday().getVideoCount());
		assertEquals(0, response.getToday().getAddedWordCount());
	}

	private WordQuiz createWordQuiz(LocalDate date, long timeInSec) {
		WordQuiz quiz = WordQuiz.builder()
			.build();
		ReflectionTestUtils.setField(quiz, "createdAt", date.atTime(10, 0));
		quiz.addLearningTime(timeInSec);
		return quiz;
	}

	private ExpressionQuiz createExpressionQuiz(LocalDate date, long timeInSec) {
		ExpressionQuiz quiz = ExpressionQuiz.builder().build();
		ReflectionTestUtils.setField(quiz, "createdAt", date.atTime(11, 0));
		quiz.addLearningTime(timeInSec);
		return quiz;
	}

	private WordQuizResult createWordQuizResult(LocalDate date) {
		WordQuizResult result = WordQuizResult.builder()
			.build();
		ReflectionTestUtils.setField(result, "createdAt", date.atTime(12, 0));
		return result;
	}

	private ExpressionQuizResult createExpressionQuizResult(LocalDate date) {
		ExpressionQuizResult result = ExpressionQuizResult.builder().build();
		ReflectionTestUtils.setField(result, "createdAt", date.atTime(13, 0));
		return result;
	}

	private VideoHistory createVideoHistory(LocalDate date) {
		VideoHistory history = VideoHistory.builder().build();
		ReflectionTestUtils.setField(history, "createdAt", date.atTime(14, 0));
		return history;
	}

	private WordbookItem createWordbookItem(LocalDate date) {
		WordbookItem item = WordbookItem.builder()
			.build();
		ReflectionTestUtils.setField(item, "createdAt", date.atTime(15, 0));
		return item;
	}
}

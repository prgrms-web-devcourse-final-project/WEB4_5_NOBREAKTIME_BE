package com.mallang.mallang_backend.domain.dashboard.service.impl;

import com.mallang.mallang_backend.domain.dashboard.dto.*;
import com.mallang.mallang_backend.domain.dashboard.service.DashboardService;
import com.mallang.mallang_backend.domain.member.entity.Level;
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
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Difficulty;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.exception.ErrorCode.LEVEL_NOT_MEASURABLE;
import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

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
    private final WordRepository wordRepository;
    private final WordbookRepository wordbookRepository;
    private final GptService gptService;

    @Override
    public StatisticResponse getStatistics(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        int watchedVideoCount = videoHistoryRepository.countByMember(member);

        DailyGoal dailyGoal = calculateDailyGoal(member);
        LevelStatus levelStatus = calculateLevelStatus(member);

        // 통합 퀴즈가 가능한지 여부 검사
        int goal = member.getWordGoal();
        List<WordbookItem> newWords = wordbookRepository.findAllByMemberAndLanguage(member, member.getLanguage()).stream()
                .flatMap(wb -> wordbookItemRepository.findAllByWordbookAndWordStatus(wb, WordStatus.NEW).stream())
                .collect(Collectors.toList());
        List<WordbookItem> reviewWords = wordbookItemRepository.findReviewTargetWords(member, LocalDateTime.now());

        boolean totalQuizAvailable = newWords.size() + reviewWords.size() >= goal;

        return new StatisticResponse(
			member.getNickname(),
			watchedVideoCount,
			dailyGoal,
			levelStatus,
			totalQuizAvailable
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

    // 레벨 측정 가능한지 확인
    private boolean checkMeasurable(Member member) {
        // 마지막 측정 이후 20개 이상의 문제를 풀었을 때 측정 가능
        int wordQuizResultCount = wordQuizResultRepository.countByWordQuiz_MemberAndCreatedAtAfter(
                member, member.getMeasuredAt());
        int expressionQuizResultCount = expressionQuizResultRepository.countByExpressionQuiz_MemberAndCreatedAtAfter(
                member, member.getMeasuredAt());
        return 20 <= wordQuizResultCount + expressionQuizResultCount;
    }

    @Override
    @Transactional
    public void updateGoal(UpdateGoalRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
        member.updateVideoGoal(request.getVideoGoal());
        member.updateWordGoal(request.getWordGoal());
        memberRepository.save(member);
    }

    @Override
    public LearningHistoryResponse getLearningStatisticsByPeriod(Long memberId, LocalDate now) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

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

        return new LearningHistoryResponse(today);
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

        long videoLearningSeconds = videoHistories.stream()
                .filter(vh -> isInRange(vh.getCreatedAt(), from, to))
                .mapToLong(vh -> Duration.parse(vh.getDuration()).getSeconds())  // ISO-8601 Duration 파싱
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

        return new LearningHistory(formatDuration(learningSeconds + videoLearningSeconds), quizCount, videoCount, addedWordCount);
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

    // 레벨 측정
    @Transactional
    @Override
    public LevelCheckResponse checkLevel(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        // 회원이 지금 측정 가능한 상태인지 체크
        if (!checkMeasurable(member)) {
            throw new ServiceException(LEVEL_NOT_MEASURABLE);
        }

        // 단어 퀴즈, 표현 퀴즈 결과에서 측정되지 않은 최대 100개의 결과 가져오기
        List<WordQuizResult> wordQuizResults = wordQuizResultRepository.findTop100ByWordQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(
                member, member.getMeasuredAt());
        List<ExpressionQuizResult> expressionQuizResults = expressionQuizResultRepository.findTop100ByExpressionQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(
                member, member.getMeasuredAt());

        // 단어 퀴즈 결과 데이터
        String wordQuizResultString = getWordsFromResults(wordQuizResults);

        // 표현 퀴즈 결과 데이터
        String expressionResultString = getExpressionsFromResults(expressionQuizResults);

        // OpenAI 결과 파싱
        String wordLevel = member.getWordLevel().toString();
        String expressionLevel = member.getExpressionLevel().toString();

        LevelCheckResponse levelCheckResponse = gptService.checkLevel(wordLevel, expressionLevel, wordQuizResultString, expressionResultString);

        member.updateWordLevel(Level.fromString(levelCheckResponse.getWordLevel()));
        member.updateExpressionLevel(Level.fromString(levelCheckResponse.getExpressionLevel()));
        member.updateMeasuredAt(LocalDateTime.now());
        memberRepository.save(member);

        return levelCheckResponse;
    }

    // 단어 퀴즈 결과에서 단어, 난이도 가져오는 메서드
    private String getWordsFromResults(List<WordQuizResult> wordQuizResults) {
        return wordQuizResults.stream()
                .map(result -> {
                    String wordText = result.getWordbookItem().getWord();
                    boolean correct = result.isCorrect();

                    // 퀴즈 결과에 해당하는 단어가 WordRepository에서 찾을 수 없는 경우 스킵
                    Optional<Word> opWord = wordRepository.findFirstByWordOrderByIdAsc(wordText);
                    if (opWord.isEmpty()) {
                        return null;
                    }

                    Difficulty difficulty = opWord.get().getDifficulty();
                    return wordText + " | " + difficulty + " | " + (correct ? "CORRECT" : "WRONG");
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    // 표현 퀴즈 결과에서 표현 가져오는 메서드
    private String getExpressionsFromResults(List<ExpressionQuizResult> expressionQuizResults) {
        return expressionQuizResults.stream()
                .map(result -> {
                    Expression expression = result.getExpression();
                    if (expression == null) {
                        return null;
                    }
                    String expressionText = expression.getSentence();
                    boolean correct = result.isCorrect();

                    return expressionText + " | " + (correct ? "CORRECT" : "WRONG");
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }
}

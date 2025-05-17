package com.mallang.mallang_backend.domain.plan.entity.domain.quiz.wordquizresult.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.plan.entity.domain.voca.wordbookitem.entity.WordbookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WordQuizResultRepository extends JpaRepository<WordQuizResult, Long> {

	int countByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime dateTime);

	List<WordQuizResult> findByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByWordbookItem(WordbookItem wordbookItem);

	List<WordQuizResult> findTop100ByWordQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);
}

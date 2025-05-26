package com.mallang.mallang_backend.domain.quiz.wordquizresult.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WordQuizResultRepository extends JpaRepository<WordQuizResult, Long> {

	int countByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime dateTime);

	List<WordQuizResult> findByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByWordbookItem(WordbookItem wordbookItem);

	List<WordQuizResult> findTop100ByWordQuiz_MemberAndCreatedAtAfterOrderByCreatedAtDesc(Member member, LocalDateTime measuredAt);

	Optional<WordQuizResult> findByWordQuizAndWordbookItem(WordQuiz wordQuiz, WordbookItem wordbookItem);
}

package com.mallang.mallang_backend.domain.quiz.wordquizresult.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WordQuizResultRepository extends JpaRepository<WordQuizResult, Long> {

	int countByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime dateTime);

	int countByWordQuiz_Member(Member member);

	List<WordQuizResult> findByWordQuiz_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

	void deleteAllByWordbookItem(WordbookItem wordbookItem);

	List<WordQuizResult> findByWordbookItem(WordbookItem wordbookItem);
}

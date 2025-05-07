package com.mallang.mallang_backend.domain.quiz.wordquiz.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WordQuizRepository extends JpaRepository<WordQuiz, Long> {
	int countByMember(Member member);

	List<WordQuiz> findByMemberAndCreatedAtAfter(Member member, LocalDateTime createdAt);
}

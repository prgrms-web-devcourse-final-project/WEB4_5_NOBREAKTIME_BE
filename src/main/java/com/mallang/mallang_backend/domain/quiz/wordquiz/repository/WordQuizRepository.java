package com.mallang.mallang_backend.domain.quiz.wordquiz.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.entity.WordQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordQuizRepository extends JpaRepository<WordQuiz, Long> {
	int countByMember(Member member);
}

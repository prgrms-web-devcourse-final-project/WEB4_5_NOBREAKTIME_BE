package com.mallang.mallang_backend.domain.quiz.expressionquiz.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.expressionquiz.entity.ExpressionQuiz;

public interface ExpressionQuizRepository extends JpaRepository<ExpressionQuiz, Long> {

	int countByMember(Member member);

	List<ExpressionQuiz> findByMemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);
}

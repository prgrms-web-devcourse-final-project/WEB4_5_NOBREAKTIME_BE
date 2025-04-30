package com.mallang.mallang_backend.domain.quiz.wordquizresult.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.quiz.wordquizresult.entity.WordQuizResult;

public interface WordQuizResultRepository extends JpaRepository<WordQuizResult, Long> {
}

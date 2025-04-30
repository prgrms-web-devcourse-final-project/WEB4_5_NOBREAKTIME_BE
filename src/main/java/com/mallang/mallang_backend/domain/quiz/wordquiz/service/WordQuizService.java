package com.mallang.mallang_backend.domain.quiz.wordquiz.service;

import java.util.List;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizItemDto;

public interface WordQuizService {
	List<WordQuizItemDto> generateWordbookQuiz(Long wordbookId, Member member);
}

package com.mallang.mallang_backend.domain.quiz.wordquiz.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;

public interface WordQuizService {
	WordQuizResponse generateWordbookQuiz(Long wordbookId, Member member);

	void saveWordbookQuizResult(WordQuizResultSaveRequest request, Member member);

	WordQuizResponse generateWordbookTotalQuiz(Member member);
}

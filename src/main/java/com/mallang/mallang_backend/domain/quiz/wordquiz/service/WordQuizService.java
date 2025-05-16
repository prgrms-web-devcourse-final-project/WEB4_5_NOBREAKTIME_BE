package com.mallang.mallang_backend.domain.quiz.wordquiz.service;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResponse;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordQuizResultSaveRequest;
import com.mallang.mallang_backend.domain.quiz.wordquiz.dto.WordbookQuizResponse;

public interface WordQuizService {
	WordbookQuizResponse generateWordbookQuiz(Long wordbookId, Member member);

	void saveWordbookQuizResult(WordQuizResultSaveRequest request, Member member);

	WordQuizResponse generateWordbookTotalQuiz(Member member);

	void saveWordbookTotalQuizResult(WordQuizResultSaveRequest request, Member member);
}

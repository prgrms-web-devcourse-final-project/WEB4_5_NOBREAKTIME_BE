package com.mallang.mallang_backend.domain.quiz.wordquiz.service.impl;

import com.mallang.mallang_backend.domain.quiz.wordquiz.service.WordQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WordQuizServiceImpl implements WordQuizService {
}

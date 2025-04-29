package com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl;

import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionBookServiceImpl implements ExpressionBookService {
}

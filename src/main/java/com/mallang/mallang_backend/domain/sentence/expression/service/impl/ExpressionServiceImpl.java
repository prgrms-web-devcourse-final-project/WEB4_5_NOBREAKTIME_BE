package com.mallang.mallang_backend.domain.sentence.expression.service.impl;

import com.mallang.mallang_backend.domain.sentence.expression.service.ExpressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionServiceImpl implements ExpressionService {
}

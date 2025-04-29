package com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.impl;

import com.mallang.mallang_backend.domain.sentence.expressionbookitem.service.ExpressionBookItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionBookItemServiceImpl implements ExpressionBookItemService {
}

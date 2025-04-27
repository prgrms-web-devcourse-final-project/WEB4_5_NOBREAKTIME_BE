package com.mallang.mallang_backend.domain.sentence.expressions.service.impl;

import com.mallang.mallang_backend.domain.sentence.expressions.service.ExpressionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionsServiceImpl implements ExpressionsService {
}

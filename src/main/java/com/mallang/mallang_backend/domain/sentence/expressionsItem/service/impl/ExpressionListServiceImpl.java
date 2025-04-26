package com.mallang.mallang_backend.domain.sentence.expressionsItem.service.impl;

import com.mallang.mallang_backend.domain.sentence.expressionsItem.service.ExpressionListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExpressionListServiceImpl implements ExpressionListService {
}

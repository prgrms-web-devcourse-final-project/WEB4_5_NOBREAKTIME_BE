package com.mallang.mallang_backend.domain.keyword.service.impl;

import com.mallang.mallang_backend.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordServiceImpl implements KeywordService {
}

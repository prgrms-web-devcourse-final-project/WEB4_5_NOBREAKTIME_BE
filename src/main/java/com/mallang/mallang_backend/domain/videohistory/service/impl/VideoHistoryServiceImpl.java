package com.mallang.mallang_backend.domain.videohistory.service.impl;

import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoHistoryServiceImpl implements VideoHistoryService {
}

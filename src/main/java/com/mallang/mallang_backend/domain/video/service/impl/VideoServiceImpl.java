package com.mallang.mallang_backend.domain.video.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.video.service.VideoService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
}

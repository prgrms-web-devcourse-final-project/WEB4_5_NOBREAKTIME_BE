package com.mallang.mallang_backend.domain.video.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mallang.mallang_backend.domain.video.service.VideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

	@Autowired
	private final VideoService videoService;
}

package com.mallang.mallang_backend.domain.video.controller;

import com.mallang.mallang_backend.domain.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("/list")
    public List<VideoResponse> getVideoList(
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "10") long maxResults
    ) {
        return videoService.getVideosByLanguage(language, maxResults);
    }
}

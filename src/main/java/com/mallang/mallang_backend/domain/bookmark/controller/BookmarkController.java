package com.mallang.mallang_backend.domain.bookmark.controller;

import com.mallang.mallang_backend.domain.bookmark.service.BookmarkService;
import com.mallang.mallang_backend.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{videoId}")
    public ResponseEntity<RsData<String>> add(
            @Login CustomUserDetails user,
            @PathVariable String videoId
    ) {
        bookmarkService.addBookmark(user.getMemberId(), videoId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RsData<>(
                        "201",
                        "북마크 추가 완료",
                        videoId
                ));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<RsData<String>> remove(
            @Login CustomUserDetails user,
            @PathVariable String videoId
    ) {
        bookmarkService.removeBookmark(user.getMemberId(), videoId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "200",
                        "북마크 제거 완료",
                        videoId
                ));
    }

    @GetMapping
    public ResponseEntity<RsData<List<VideoResponse>>> getAll(
            @Login CustomUserDetails user
    ) {
        List<Videos> bookmarks = bookmarkService.getBookmarks(user.getMemberId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new RsData<>(
                        "200",
                        "북마크 목록 조회 성공",
                        bookmarks.stream()
                                .map(video -> VideoResponse.from(video, true))
                                .toList()
                ));
    }
}
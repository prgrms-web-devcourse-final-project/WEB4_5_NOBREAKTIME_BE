package com.mallang.mallang_backend.domain.plan.entity.domain.bookmark.controller;

import com.mallang.mallang_backend.domain.plan.entity.domain.bookmark.service.BookmarkService;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.dto.VideoResponse;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.dto.RsData;
import com.mallang.mallang_backend.global.filter.login.CustomUserDetails;
import com.mallang.mallang_backend.global.filter.login.Login;
import com.mallang.mallang_backend.global.swagger.PossibleErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Tag(name = "Bookmark", description = "영상 북마크 관련 API")
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 영상 북마크 추가
     *
     * @param user    로그인한 사용자
     * @param videoId 북마크할 영상 ID
     * @return 북마크 추가 결과
     */
    @Operation(summary = "영상 북마크 추가", description = "특정 영상을 북마크에 추가합니다.")
    @ApiResponse(responseCode = "201", description = "북마크 추가 완료")
    @PossibleErrors({MEMBER_NOT_FOUND, BOOKMARK_ALREADY_EXISTS})
    @PostMapping("/{videoId}")
    public ResponseEntity<RsData<String>> add(
            @Parameter(hidden = true)
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

    /**
     * 영상 북마크 제거
     *
     * @param user    로그인한 사용자
     * @param videoId 북마크에서 제거할 영상 ID
     * @return 북마크 제거 결과
     */
    @Operation(summary = "영상 북마크 제거", description = "특정 영상을 북마크에서 제거합니다.")
    @ApiResponse(responseCode = "200", description = "북마크 제거 완료")
    @PossibleErrors({BOOKMARK_NOT_FOUND})
    @DeleteMapping("/{videoId}")
    public ResponseEntity<RsData<String>> remove(
            @Parameter(hidden = true)
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

    /**
     * 사용자가 북마크한 영상 전체 조회
     *
     * @param user 로그인한 사용자
     * @return 북마크한 영상 목록
     */
    @Operation(summary = "북마크 영상 전체 조회", description = "사용자가 북마크한 모든 영상을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    @PossibleErrors({MEMBER_NOT_FOUND})
    @GetMapping
    public ResponseEntity<RsData<List<VideoResponse>>> getAll(
            @Parameter(hidden = true)
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
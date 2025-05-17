package com.mallang.mallang_backend.domain.bookmark.service;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;

import java.util.List;

public interface BookmarkService {

    void addBookmark(Long memberId, String videoId);

    void removeBookmark(Long memberId, String videoId);

    List<Videos> getBookmarks(Long memberId);



}

package com.mallang.mallang_backend.domain.bookmark.service.impl;

import com.mallang.mallang_backend.domain.bookmark.entity.Bookmark;
import com.mallang.mallang_backend.domain.bookmark.repository.BookmarkRepository;
import com.mallang.mallang_backend.domain.bookmark.service.BookmarkService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.mallang.mallang_backend.global.exception.ErrorCode.VIDEO_ID_SEARCH_FAILED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;

    @Override
    public void addBookmark(Long memberId, String videoId) {
        if (bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)) return;

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

        bookmarkRepository.save(new Bookmark(member, video));
    }

    @Override
    public void removeBookmark(Long memberId, String videoId) {
        bookmarkRepository.deleteByMemberIdAndVideosId(memberId, videoId);
    }

    @Override
    public List<Videos> getBookmarks(Long memberId) {
        return bookmarkRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(Bookmark::getVideos)
                .toList();
    }
}

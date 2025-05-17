package com.mallang.mallang_backend.domain.bookmark.service.impl;

import com.mallang.mallang_backend.domain.bookmark.entity.Bookmark;
import com.mallang.mallang_backend.domain.bookmark.repository.BookmarkRepository;
import com.mallang.mallang_backend.domain.bookmark.service.BookmarkService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.service.impl.VideoServiceImpl;
import com.mallang.mallang_backend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final VideoServiceImpl videoService;

    @Override
    @Transactional
    public void addBookmark(Long memberId, String videoId) {
        if (bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)) {
            throw new ServiceException(BOOKMARK_ALREADY_EXISTS);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        Videos video = videoService.saveVideoIfAbsent(videoId);

        bookmarkRepository.save(new Bookmark(member, video));
    }

    @Override
    @Transactional
    public void removeBookmark(Long memberId, String videoId) {
        if (!bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)) {
            throw new ServiceException(BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMemberIdAndVideoId(memberId, videoId);
    }

    @Override
    public List<Videos> getBookmarks(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        return bookmarkRepository.findAllWithVideoByMemberId(memberId)
                .stream()
                .map(Bookmark::getVideos)
                .toList();
    }
}

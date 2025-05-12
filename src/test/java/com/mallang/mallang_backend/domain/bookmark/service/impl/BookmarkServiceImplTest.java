package com.mallang.mallang_backend.domain.bookmark.service.impl;

import com.mallang.mallang_backend.domain.bookmark.entity.Bookmark;
import com.mallang.mallang_backend.domain.bookmark.repository.BookmarkRepository;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.service.impl.VideoServiceImpl;
import com.mallang.mallang_backend.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceImplTest {

    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private VideoServiceImpl videoService;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Test
    @DisplayName("북마크 추가 성공")
    void addBookmark_success() {
        Long memberId = 1L;
        String videoId = "abc123";
        Member member = mock(Member.class);
        Videos video = mock(Videos.class);

        given(bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)).willReturn(false);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(videoService.saveVideoIfAbsent(videoId)).willReturn(video);

        bookmarkService.addBookmark(memberId, videoId);

        then(bookmarkRepository).should().save(any(Bookmark.class));
    }

    @Test
    @DisplayName("이미 북마크된 영상 추가 시 예외")
    void addBookmark_duplicate_throwsException() {
        Long memberId = 1L;
        String videoId = "abc123";
        given(bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)).willReturn(true);

        assertThatThrownBy(() -> bookmarkService.addBookmark(memberId, videoId))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("북마크 삭제 성공")
    void removeBookmark_success() {
        Long memberId = 1L;
        String videoId = "abc123";
        given(bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)).willReturn(true);

        bookmarkService.removeBookmark(memberId, videoId);

        then(bookmarkRepository).should().deleteByMemberIdAndVideoId(memberId, videoId);
    }

    @Test
    @DisplayName("존재하지 않는 북마크 삭제 시 예외")
    void removeBookmark_notFound_throwsException() {
        Long memberId = 1L;
        String videoId = "abc123";
        given(bookmarkRepository.existsByMemberIdAndVideosId(memberId, videoId)).willReturn(false);

        assertThatThrownBy(() -> bookmarkService.removeBookmark(memberId, videoId))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("북마크 전체 조회 성공")
    void getBookmarks_success() {
        Long memberId = 1L;
        Member member = mock(Member.class);
        Bookmark bookmark = mock(Bookmark.class);
        Videos video = mock(Videos.class);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(bookmark.getVideos()).willReturn(video);
        given(bookmarkRepository.findAllWithVideoByMemberId(memberId)).willReturn(List.of(bookmark));

        List<Videos> result = bookmarkService.getBookmarks(memberId);

        assertEquals(1, result.size());
        assertEquals(video, result.get(0));
    }

    @Test
    @DisplayName("회원이 존재하지 않을 경우 북마크 전체 조회 시 예외")
    void getBookmarks_memberNotFound() {
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.getBookmarks(memberId))
                .isInstanceOf(ServiceException.class);
    }
}


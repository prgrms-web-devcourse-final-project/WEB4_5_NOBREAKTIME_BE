package com.mallang.mallang_backend.domain.bookmark.repository;

import com.mallang.mallang_backend.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    boolean existsByMemberIdAndVideosId(Long memberId, String videoId);
    void deleteByMemberIdAndVideosId(Long memberId, String videoId);
}

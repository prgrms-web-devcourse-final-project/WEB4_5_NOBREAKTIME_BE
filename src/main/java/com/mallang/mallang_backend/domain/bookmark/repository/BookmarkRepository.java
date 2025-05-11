package com.mallang.mallang_backend.domain.bookmark.repository;

import com.mallang.mallang_backend.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.videos WHERE b.member.id = :memberId ORDER BY b.createdAt DESC")
    List<Bookmark> findAllWithVideoByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndVideosId(Long memberId, String videoId);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.member.id = :memberId AND b.videos.id = :videoId")
    void deleteByMemberIdAndVideoId(@Param("memberId") Long memberId, @Param("videoId") String videoId);

}

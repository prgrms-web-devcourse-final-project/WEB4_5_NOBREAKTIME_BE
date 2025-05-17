package com.mallang.mallang_backend.domain.plan.entity.domain.videohistory.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.plan.entity.domain.videohistory.entity.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {

    // 최근 5개
    List<VideoHistory> findTop5ByMemberOrderByLastViewedAtDesc(Member member);

    int countByMember(Member member);

    // 오늘 본 영상 갯수
    int countByMemberAndCreatedAtAfter(Member member, LocalDateTime todayStart);

    List<VideoHistory> findByMemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);

    Optional<VideoHistory> findByMemberAndVideos(Member member, Videos videos);

    // 페이징 조회
    List<VideoHistory> findAllByMemberOrderByLastViewedAtDesc(Member member);

    List<VideoHistory> findAllByMemberOrderByLastViewedAtAsc(Member member);
}

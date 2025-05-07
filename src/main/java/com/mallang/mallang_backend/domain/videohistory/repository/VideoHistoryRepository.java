package com.mallang.mallang_backend.domain.videohistory.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {

    // 최근 5개
    List<VideoHistory> findTop5ByMemberOrderByCreatedAtDesc(Member member);

    // 전체 조회
    List<VideoHistory> findAllByMemberOrderByCreatedAtDesc(Member member);

    int countByMember(Member member);

    // 오늘 본 영상 갯수
    int countByMemberAndCreatedAtAfter(Member member, LocalDateTime todayStart);

    List<VideoHistory> findByMemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);
}

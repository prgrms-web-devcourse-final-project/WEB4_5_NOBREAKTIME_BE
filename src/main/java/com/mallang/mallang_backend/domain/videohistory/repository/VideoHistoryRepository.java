package com.mallang.mallang_backend.domain.videohistory.repository;

import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoHistoryRepository extends JpaRepository<VideoHistory, VideoHistoryId> {
    List<VideoHistory> findTop5ByIdMemberIdOrderByCreatedAtDesc(Long memberId);
    List<VideoHistory> findAllByIdMemberIdOrderByCreatedAtDesc(Long memberId);
}

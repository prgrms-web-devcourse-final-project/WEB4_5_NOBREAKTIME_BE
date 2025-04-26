package com.mallang.mallang_backend.domain.videohistory.repository;

import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
}

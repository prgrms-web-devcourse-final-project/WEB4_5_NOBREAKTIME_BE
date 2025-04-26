package com.mallang.mallang_backend.domain.video.repository;

import com.mallang.mallang_backend.domain.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}

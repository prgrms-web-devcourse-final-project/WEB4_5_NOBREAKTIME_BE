package com.mallang.mallang_backend.domain.video.subtitle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;

public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {
}

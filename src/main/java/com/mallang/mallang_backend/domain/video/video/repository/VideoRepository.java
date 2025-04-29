package com.mallang.mallang_backend.domain.video.video.repository;

import java.util.Optional;

import com.mallang.mallang_backend.domain.video.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, String> {
}

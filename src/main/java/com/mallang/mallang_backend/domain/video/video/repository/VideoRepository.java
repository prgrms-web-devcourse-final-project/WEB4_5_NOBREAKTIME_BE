package com.mallang.mallang_backend.domain.video.video.repository;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Videos, String> {
}

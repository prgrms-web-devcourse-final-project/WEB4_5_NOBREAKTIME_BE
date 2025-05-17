package com.mallang.mallang_backend.domain.plan.entity.domain.video.video.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Videos, String> {
	List<Videos> findByIdIn(List<String> videoIds);
}

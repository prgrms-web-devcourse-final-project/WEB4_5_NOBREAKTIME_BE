package com.mallang.mallang_backend.domain.video.video.repository;

import java.util.Collection;
import java.util.List;

import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Videos, String> {
	List<Videos> findByIdIn(List<java.lang.String> videoIds);
}

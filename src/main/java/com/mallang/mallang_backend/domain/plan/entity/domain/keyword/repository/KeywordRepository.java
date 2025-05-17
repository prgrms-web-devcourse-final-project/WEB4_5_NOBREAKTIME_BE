package com.mallang.mallang_backend.domain.plan.entity.domain.keyword.repository;

import com.mallang.mallang_backend.domain.plan.entity.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
	List<Keyword> findAllByVideosId(String videoId);
}

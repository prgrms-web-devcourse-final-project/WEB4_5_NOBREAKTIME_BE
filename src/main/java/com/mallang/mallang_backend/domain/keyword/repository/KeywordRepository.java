package com.mallang.mallang_backend.domain.keyword.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
	List<Keyword> findAllByVideosId(String videoId);
}

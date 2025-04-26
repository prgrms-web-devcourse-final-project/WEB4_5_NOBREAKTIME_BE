package com.mallang.mallang_backend.domain.keyword.repository;

import com.mallang.mallang_backend.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}

package com.mallang.mallang_backend.domain.voca.wordbook.repository;

import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookRepository extends JpaRepository<Wordbook, Long> {
}

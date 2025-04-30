package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;

public interface WordbookItemRepository extends JpaRepository<WordbookItem, Long> {
	Optional<WordbookItem> findByWordbookIdAndWord(long wordbookId, String word);
}

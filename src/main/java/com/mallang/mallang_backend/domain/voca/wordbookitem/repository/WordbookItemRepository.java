package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;

public interface WordbookItemRepository extends JpaRepository<WordbookItem, Long>, WordbookItemRepositoryCustom {
	List<WordbookItem> findAllByWordbook(Wordbook wordbook);

	List<WordbookItem> findAllByWordbookAndWordStatus(Wordbook wordbook, WordStatus wordStatus);
}

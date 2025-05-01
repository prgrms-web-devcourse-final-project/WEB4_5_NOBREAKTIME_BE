package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;

public interface WordbookItemRepository extends JpaRepository<WordbookItem, Long> {
	Optional<WordbookItem> findByWordbookIdAndWord(long wordbookId, String word);
	Optional<WordbookItem> findByWordbookAndWord(Wordbook wordbook, String word);
	List<WordbookItem> findAllByWordbook(Wordbook wordbook);
	void deleteAllByWordbookId(Long wordbookId);

	List<WordbookItem> findAllByWordbookAndWordStatus(Wordbook wordbook, WordStatus wordStatus);
}

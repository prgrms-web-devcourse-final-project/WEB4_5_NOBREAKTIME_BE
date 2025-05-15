package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WordbookItemRepository extends JpaRepository<WordbookItem, Long>, WordbookItemRepositoryCustom {
	Optional<WordbookItem> findByWordbookIdAndWord(long wordbookId, String word);
	Optional<WordbookItem> findByWordbookAndWord(Wordbook wordbook, String word);
	List<WordbookItem> findAllByWordbook(Wordbook wordbook);
	void deleteAllByWordbookId(Long wordbookId);
	List<WordbookItem> findAllByWordbookAndWordStatus(Wordbook wordbook, WordStatus wordStatus);
	List<WordbookItem> findByWordbook_MemberAndCreatedAtAfter(Member member, LocalDateTime localDateTime);
	List<WordbookItem> findByWordbook_MemberAndWordContaining(Member member, String keyword);

    List<WordbookItem> findAllByWordbookIdIn(List<Long> wordbookIds);
}

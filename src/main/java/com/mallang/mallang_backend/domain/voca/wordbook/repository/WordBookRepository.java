package com.mallang.mallang_backend.domain.voca.wordbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;

public interface WordbookRepository extends JpaRepository<Wordbook, Long> {
	Optional<Wordbook> findByIdAndMember(Long wordbookId, Member member);
}

package com.mallang.mallang_backend.domain.voca.wordbook.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.global.common.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordbookRepository extends JpaRepository<Wordbook, Long> {
	Optional<Wordbook> findByIdAndMember(Long wordbookId, Member member);
	List<Wordbook> findAllByMember(Member member);
	List<Wordbook> findAllByMemberId(Long memberId);
	Optional<Wordbook> findByIdAndMemberId(Long wordbookId, Long memberId);

	List<Wordbook> findByMember(Member member);

	List<Wordbook> findAllByMemberIdAndLanguage(Long memberId, Language language);

	Optional<Wordbook> findByMemberAndName(Member member, String name);

    Optional<Wordbook> findByMemberAndNameAndLanguage(Member member, String wordbookName, Language language);

	boolean existsByMemberAndName(Member member, String name);

	List<Wordbook> findAllByMemberAndLanguage(Member member, Language language);
}
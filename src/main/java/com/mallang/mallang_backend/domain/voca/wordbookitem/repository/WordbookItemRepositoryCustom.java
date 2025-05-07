package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;

import java.time.LocalDateTime;
import java.util.List;

public interface WordbookItemRepositoryCustom {
	List<WordbookItem> findReviewTargetWords(Member member, LocalDateTime now);
}

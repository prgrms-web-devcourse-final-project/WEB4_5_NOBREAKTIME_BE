package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.QWordbookItem;
import org.springframework.stereotype.Repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WordbookItemRepositoryCustomImpl implements WordbookItemRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<WordbookItem> findReviewTargetWords(Member member, LocalDateTime now) {
		QWordbookItem wordbookItem = QWordbookItem.wordbookItem;

		return queryFactory.selectFrom(wordbookItem)
			.where(
				wordbookItem.wordbook.member.eq(member),
				wordbookItem.wordStatus.ne(WordStatus.NEW),
				isReviewDue(wordbookItem.wordStatus, wordbookItem.lastStudiedAt, now)
			)
			.fetch();
	}

	private BooleanBuilder isReviewDue(
		EnumPath<WordStatus> status,
		DateTimePath<LocalDateTime> studiedAt,
		LocalDateTime now
	) {
		BooleanBuilder builder = new BooleanBuilder();

		DateTemplate<LocalDate> studiedDate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", studiedAt);
		LocalDate today = now.toLocalDate();

		builder.or(status.eq(WordStatus.WRONG)
			.and(studiedDate.loe(today.minusDays(1))));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_1)
			.and(studiedDate.loe(today.minusDays(7))));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_2)
			.and(studiedDate.loe(today.minusDays(30))));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_3)
			.and(studiedDate.loe(today.minusDays(90))));
		builder.or(status.eq(WordStatus.CORRECT)
			.and(studiedDate.loe(today.minusDays(180))));

		return builder;
	}

}

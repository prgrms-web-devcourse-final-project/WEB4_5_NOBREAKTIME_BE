package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.QWordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WordbookItemRepositoryImpl implements WordbookItemRepositoryCustom {

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

	/**
	 * 단어의 상태별 복습 대상 조건을 구성하는 BooleanExpression.
	 */
	private BooleanBuilder isReviewDue(
		EnumPath<WordStatus> status,
		DateTimePath<LocalDateTime> studiedAt,
		LocalDateTime now
	) {
		BooleanBuilder builder = new BooleanBuilder();

		builder.or(status.eq(WordStatus.WRONG)
			.and(dateTimePlusDays(studiedAt, 1).loe(now)));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_1)
			.and(dateTimePlusDays(studiedAt, 7).loe(now)));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_2)
			.and(dateTimePlusDays(studiedAt, 30).loe(now)));
		builder.or(status.eq(WordStatus.REVIEW_COUNT_3)
			.and(dateTimePlusDays(studiedAt, 90).loe(now)));
		builder.or(status.eq(WordStatus.CORRECT)
			.and(dateTimePlusDays(studiedAt, 180).loe(now)));

		return builder;
	}

	/**
	 * DATE_ADD 함수로 LocalDateTime에 일수를 더하는 표현식 생성.
	 */
	private DateTimeExpression<LocalDateTime> dateTimePlusDays(DateTimePath<LocalDateTime> dateTimePath, int days) {
		return Expressions.dateTimeTemplate(
			LocalDateTime.class,
			"DATE_ADD({0}, INTERVAL {1} DAY)",
			dateTimePath,
			days
		);
	}
}

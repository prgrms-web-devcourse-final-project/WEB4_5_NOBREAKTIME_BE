package com.mallang.mallang_backend.domain.voca.wordbookitem.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.QWordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordStatus.*;

@Repository
@RequiredArgsConstructor
public class WordbookItemRepositoryCustomImpl implements WordbookItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<WordbookItem> findReviewTargetWords(Member member, LocalDateTime now) {
        QWordbookItem wordbookItem = QWordbookItem.wordbookItem;

        return queryFactory.selectFrom(wordbookItem).where(
                // 단어장의 소유자가 현재 회원과 일치하는 단어
                wordbookItem.wordbook.member.eq(member),
                // 새로운 단어(NEW) 상태가 아닌 단어
                wordbookItem.wordStatus.ne(NEW),
                // 마지막 복습 날짜가 각 상태별 복습 주기에 도달한 단어
                isReviewDue(wordbookItem.wordStatus, wordbookItem.lastStudiedAt, now)).fetch();
    }

    private BooleanBuilder isReviewDue(EnumPath<WordStatus> status, DateTimePath<LocalDateTime> studiedAt, LocalDateTime now) {
        // 여러 조건을 or로 연결하여 복잡한 조건을 쉽게 표현
        BooleanBuilder builder = new BooleanBuilder();

        // Expressions.dateTemplate()를 이용하여 LocalDateTime에서 LocalDate를 추출하여 비교
        DateTemplate<LocalDate> studiedDate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", studiedAt);
        LocalDate today = now.toLocalDate();

        // 모든 WordStatus에 대해 복습 주기 검사
        for (WordStatus wordStatus : WordStatus.values()) {
            Duration interval = wordStatus.getReviewIntervalDuration();
            // 복습 주기가 있는 상태만 처리 (NEW, MASTERED 제외)
            // 현재부터 복습 주기만큼 이전 날짜 기준 마지막 학습 시간이 작거나 같은 단어 (loe)
            if (!interval.isZero()) {
                builder.or(status.eq(wordStatus)
                        .and(studiedDate.loe(today.minusDays(interval.toDays()))));
            }
        }
        return builder;
    }
}

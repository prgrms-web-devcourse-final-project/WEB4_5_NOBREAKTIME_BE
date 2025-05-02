package com.mallang.mallang_backend.domain.member.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static com.mallang.mallang_backend.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 지정된 날짜(threshold) 이전에 탈퇴 처리된 회원을 일괄 삭제
     *
     * JPA 단건 조회+삭제 대신, bulk delete 사용: 조건에 맞는 모든 회원을 한 번에 삭제
     *
     * @param withdrawalThreshold 삭제 기준이 되는 탈퇴일(이 날짜 이전 탈퇴 회원 삭제)
     * @return 삭제된 회원 수
     * @throws PersistenceException DB 연결 등 일시적 장애 발생 가능 -> 이는 재시도 로직으로 처리
     */
    public long bulkDeleteExpiredMembers(LocalDateTime withdrawalThreshold) {
        return queryFactory
                .delete(member)
                .where(member.withdrawalDate.before(withdrawalThreshold))
                .execute();
    }
}

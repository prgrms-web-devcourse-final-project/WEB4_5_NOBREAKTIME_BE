package com.mallang.mallang_backend.domain.member.repository;

import com.mallang.mallang_backend.domain.member.dto.MemberSimpleInfo;
import com.mallang.mallang_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPlatformId(String platformId);

    @Query("select new com.mallang.mallang_backend.domain.member.dto.MemberSimpleInfo(m.id, m.subscriptionType, m.language) from Member m where m.platformId = :platformId")
    MemberSimpleInfo findMemberGrantedInfo(@Param("platformId") String platformId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}

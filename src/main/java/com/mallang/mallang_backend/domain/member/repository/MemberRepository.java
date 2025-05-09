package com.mallang.mallang_backend.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPlatformId(String platformId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPlatformId(String platformId);
}

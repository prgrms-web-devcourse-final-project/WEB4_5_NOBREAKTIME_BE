package com.mallang.mallang_backend.domain.member.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByPlatformId(String platformId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByPlatformId(String platformId);
}

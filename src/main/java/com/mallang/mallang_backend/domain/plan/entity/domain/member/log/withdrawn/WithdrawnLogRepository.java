package com.mallang.mallang_backend.domain.plan.entity.domain.member.log.withdrawn;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawnLogRepository extends JpaRepository<WithdrawnLog, Long> {
    WithdrawnLog findByOriginalPlatformId(String originalPlatformId);

    boolean existsByOriginalPlatformId(String originalPlatformId);
}

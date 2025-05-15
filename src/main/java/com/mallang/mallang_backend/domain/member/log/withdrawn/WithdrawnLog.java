package com.mallang.mallang_backend.domain.member.log.withdrawn;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 30일 후 자동 삭제가 필요함
 */
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WithdrawnLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId; // Member 엔티티 대신 ID만 저장, 멤버 삭제 시에도 삭제되지 않도록

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String originalPlatformId; // 유니크 제약 설정 -> 검색 속도 증가

    @Builder
    private WithdrawnLog(Long memberId,
                         String originalPlatformId,
                         String uuid) {
        this.memberId = memberId;
        this.originalPlatformId = originalPlatformId;
        this.uuid = uuid;
    }
}

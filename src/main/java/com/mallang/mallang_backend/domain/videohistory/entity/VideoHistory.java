package com.mallang.mallang_backend.domain.videohistory.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "video_history")
public class VideoHistory extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Videos videos;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    @Builder
    public VideoHistory(Member member, Videos videos) {
        this.member = member;
        this.videos = videos;
        this.lastViewedAt = LocalDateTime.now();
    }

    /**
     * 기록이 이미 존재할 때, 마지막 조회 시간을 현재 시각으로 갱신
     */
    public void updateTimestamp() {
        this.lastViewedAt = LocalDateTime.now();
    }


    /**
     * 시청한 영상의 길이를 반환합니다.
     * @return 시청한 영상의 길이
     */
    public String getDuration() {
        return videos.getDuration();
    }
}

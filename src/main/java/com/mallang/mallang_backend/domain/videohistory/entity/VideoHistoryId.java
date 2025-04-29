package com.mallang.mallang_backend.domain.videohistory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class VideoHistoryId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "video_id")
    private String videoId;
}
package com.mallang.mallang_backend.domain.video.subtitle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;

public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {

    @Query("SELECT DISTINCT s FROM Subtitle s LEFT JOIN FETCH s.keywords WHERE s.videos = :video")
    List<Subtitle> findAllByVideosFetchKeywords(@Param("video") Videos video);

    List<Subtitle> findByIdIn(List<Long> ids);
}

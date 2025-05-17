package com.mallang.mallang_backend.domain.video.subtitle.repository;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {

    @Query("SELECT DISTINCT s FROM Subtitle s LEFT JOIN FETCH s.keywords WHERE s.videos.id = :videoId")
    List<Subtitle> findAllByVideosFetchKeywords(@Param("videoId") String videoId);

    List<Subtitle> findByIdIn(List<Long> ids);
}

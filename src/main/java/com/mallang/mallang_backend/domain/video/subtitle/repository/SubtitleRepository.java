package com.mallang.mallang_backend.domain.video.subtitle.repository;

import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {

    @Query("SELECT s FROM Subtitle s JOIN FETCH s.keywords WHERE s.videos = :video")
    List<Subtitle> findAllByVideosFetchKeywords(@Param("video") Videos video);

    List<Subtitle> findByIdIn(List<Long> ids);
}

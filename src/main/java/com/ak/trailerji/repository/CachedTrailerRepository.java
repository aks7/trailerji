package com.ak.trailerji.repository;

import com.ak.trailerji.entity.CachedTrailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface CachedTrailerRepository extends JpaRepository<CachedTrailer, Long> {

    Logger log = LoggerFactory.getLogger(CachedTrailerRepository.class);

    Page<CachedTrailer> findByOrderByPublishedAtDesc(Pageable pageable);
    Optional<CachedTrailer> findByVideoId(String videoId);
    Page<CachedTrailer> findByChannelIdInOrderByPublishedAtDesc(List<String> channelIds, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            INSERT INTO cached_trailers (video_id, title, description, channel_title, published_at, thumbnail_url, channel_id)
            VALUES (:videoId, :title, :description, :channelTitle, :publishedAt, :thumbnailUrl, :channelId)
            ON CONFLICT (video_id)
            DO UPDATE SET title = EXCLUDED.title,
                          description = EXCLUDED.description,
                          channel_title = EXCLUDED.channel_title,
                          published_at = EXCLUDED.published_at,
                          thumbnail_url = EXCLUDED.thumbnail_url,
                          channel_id = EXCLUDED.channel_id
            """, nativeQuery = true)
    void upsertTrailer(@Param("videoId") String videoId,
                       @Param("title") String title,
                       @Param("description") String description,
                       @Param("channelTitle") String channelTitle,
                       @Param("publishedAt") LocalDateTime publishedAt,
                       @Param("thumbnailUrl") String thumbnailUrl,
                       @Param("channelId") String channelId);
}
package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {
    List<Article> findByStatusOrderByPublishDateDesc(String status);

    long countByPinnedTrue();

    long countByRecommendedTrue();

    @Query("SELECT a FROM Article a WHERE a.status = :status ORDER BY CASE WHEN a.pinned = true THEN 0 ELSE 1 END, a.publishDate DESC")
    List<Article> findByStatusPinnedFirst(@Param("status") String status);

    @Query("SELECT a.tag, SUM(COALESCE(a.viewCount, 0)) FROM Article a WHERE a.status = 'published' AND a.tag IS NOT NULL AND a.tag <> '' GROUP BY a.tag ORDER BY SUM(COALESCE(a.viewCount, 0)) DESC")
    List<Object[]> findHotTagsWithHeat(Pageable pageable);

    @Query("SELECT COALESCE(SUM(a.viewCount), 0) FROM Article a WHERE a.status = 'published'")
    Long sumPublishedViewCount();
}
